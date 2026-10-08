package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.sortCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock

@Service
class CustomCaseListQueryService(
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val userCustomCaseListRepository: UserCustomCaseListRepository,
  private val customCaseListOrchestrationService: CustomCaseListOrchestrationService,
  private val clock: Clock,
) {
  fun getCustomCaseList(): ApiResponseDto<List<CaseDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseIds = userCustomCaseListRepository.findAllBySasUserId(user.id).map { it.sasCaseId }
    if (caseIds.isEmpty()) return toApiResponseDto(data = emptyList())

    val caseEntities = caseRepository.findAllWithIdentifiersByIdIn(caseIds)
    val crns = caseEntities.map { it.latestCrn() }
    val orchestrationResult = customCaseListOrchestrationService.getCaseAccessAndSummaries(user.username, crns)
    val userCaseAccess = getUserCaseAccessOrThrow(orchestrationResult)

    val limitedAccessCrns = getLimitedAccessCrns(crns, userCaseAccess)
    val caseSummariesByCrn = orchestrationResult.data.caseSummaries.associateBy { it.crn }
    val caseDtos = caseEntities.map {
      val crn = it.latestCrn()
      if (crn in limitedAccessCrns) toLimitedCaseDto(crn) else it.toCustomCaseListCaseDto(caseSummariesByCrn[crn])
    }
    return toApiResponseDto(data = caseDtos.sortCases(clock), upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun getUserCaseAccessOrThrow(
    orchestrationResult: OrchestrationResultDto<CustomCaseListOrchestrationDto>,
  ): UserCaseAccess {
    orchestrationResult.upstreamFailures.firstOrNull { it.callKey == GET_USER_ACCESS }?.let {
      throw UpstreamFailureException(UpstreamFailureTransformer.toUpstreamFailureDto(it))
    }
    return requireNotNull(orchestrationResult.data.userCaseAccess)
  }

  private fun CaseEntity.toCustomCaseListCaseDto(caseSummary: CaseSummary?) = toCaseDto(
    caseEntity = this,
    crn = latestCrn(),
    prisonNumber = latestPrisonNumber(),
    riskLevel = roshLevelCode?.let { RiskLevel.findByCode(it) },
    pncReference = null,
    assignedTo = toAssignedToDto(),
    limitedAccess = caseSummary?.let { it.currentExclusion || it.currentRestriction },
  )

  private fun getLimitedAccessCrns(crns: List<String>, userCaseAccess: UserCaseAccess): Set<String> {
    val accessByCrn = userCaseAccess.access.associateBy { it.crn }
    return crns.filter { crn -> accessByCrn[crn]?.let { it.userExcluded || it.userRestricted } ?: true }.toSet()
  }
}
