package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.filterCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.sortCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.time.LocalDate

@Service
class CustomCaseListQueryService(
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val userCustomCaseListRepository: UserCustomCaseListRepository,
  private val customCaseListOrchestrationService: CustomCaseListOrchestrationService,
  private val limitedAccessService: LimitedAccessService,
  private val clock: Clock,
) {
  fun getCustomCaseList(
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
    teamCode: String? = null,
    peopleType: PeopleType? = null,
  ): ApiResponseDto<List<CaseDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseIds = userCustomCaseListRepository.findAllBySasUserId(user.id).map { it.sasCaseId }
    if (caseIds.isEmpty()) return toApiResponseDto(data = emptyList())

    val caseEntities = caseRepository.findAllWithIdentifiersByIdIn(caseIds)
    val crns = caseEntities.map { it.latestCrn() }
    val orchestrationResult = customCaseListOrchestrationService.getCaseAccessAndSummaries(user.username, crns)
    limitedAccessService.throwIfUserAccessFailed(orchestrationResult.upstreamFailures)

    val crnsToUserAccess = limitedAccessService.getCrnToUserAccess(orchestrationResult.data.caseAccess)
    val caseSummariesByCrn = orchestrationResult.data.caseSummaries.associateBy { it.crn }
    val caseDtos = caseEntities.map { entity ->
      val crn = entity.latestCrn()
      val caseSummary = caseSummariesByCrn[crn]
      val caseTeamCode = caseSummary?.manager?.team?.code
      val isLao = limitedAccessService.isLao(caseSummary)
      val userAccess = crnsToUserAccess[crn]
      when (userAccess) {
        UserAccess.FULL -> {
          toCaseDto(
            caseEntity = entity,
            crn = entity.latestCrn(),
            prisonNumber = entity.latestPrisonNumber(),
            riskLevel = entity.roshLevelCode?.let { RiskLevel.findByCode(it) },
            pncReference = null,
            assignedTo = entity.toAssignedToDto(),
            limitedAccess = isLao,
            teamCode = caseTeamCode,
          )
        }

        UserAccess.LIMITED, UserAccess.UNKNOWN, null -> toLimitedCaseDto(
          crn,
          teamCode = caseTeamCode,
          assignedTo = entity.toAssignedToDto(),
        )
      }
    }.filterCases(
      searchTerm = searchTerm,
      riskLevel = riskLevel,
      teamCode = teamCode,
      username = null,
      peopleType = peopleType,
    ).sortCases(
      today = LocalDate.now(clock),
    )
    return toApiResponseDto(
      data = caseDtos,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCustomCaseListCrns(): ApiResponseDto<List<String>> {
    val user = userService.authorizeAndRetrieveUser()
    return toApiResponseDto(data = userCustomCaseListRepository.findLatestCrnsBySasUserId(user.id))
  }
}
