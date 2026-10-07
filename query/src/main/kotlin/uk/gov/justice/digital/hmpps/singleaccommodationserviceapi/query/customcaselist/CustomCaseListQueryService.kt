package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.ProbationAccessControlService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
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
  private val probationAccessControlService: ProbationAccessControlService,
  private val clock: Clock,
) {
  fun getCustomCaseList(): ApiResponseDto<List<CaseDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseIds = userCustomCaseListRepository.findAllBySasUserId(user.id).map { it.sasCaseId }
    if (caseIds.isEmpty()) return toApiResponseDto(data = emptyList())

    val caseEntities = caseRepository.findAllWithIdentifiersByIdIn(caseIds)
    val limitedAccessCrns = getLimitedAccessCrns(user.username, caseEntities.map { it.latestCrn() })
    val caseDtos = caseEntities.map {
      if (it.latestCrn() in limitedAccessCrns) toLimitedCaseDto(it.latestCrn()) else it.toCustomCaseListCaseDto()
    }
    return toApiResponseDto(data = caseDtos.sortCases(clock))
  }

  private fun CaseEntity.toCustomCaseListCaseDto() = toCaseDto(
    caseEntity = this,
    crn = latestCrn(),
    prisonNumber = latestPrisonNumber(),
    riskLevel = roshLevelCode?.let { RiskLevel.findByCode(it) },
    // TODO: investigate ways of populating the below fields (no values for these in sas_case)
    pncReference = null,
    assignedTo = null,
    limitedAccess = null,
  )

  private fun getLimitedAccessCrns(username: String, crns: List<String>): Set<String> {
    val accessByCrn = probationAccessControlService.getUserAccess(username, crns).access.associateBy { it.crn }
    return crns.filter { crn -> accessByCrn[crn]?.let { it.userExcluded || it.userRestricted } ?: true }.toSet()
  }
}
