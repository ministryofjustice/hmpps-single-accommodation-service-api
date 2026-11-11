package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.usercaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCaseListCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCaseListEntryRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.filterCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.obfuscateLimitedAccessCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.sortCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.time.LocalDate

@Service
class UserCaseListQueryService(
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val userCaseListEntryRepository: UserCaseListEntryRepository,
  private val userCaseListOrchestrationService: UserCaseListOrchestrationService,
  private val limitedAccessService: LimitedAccessService,
  private val clock: Clock,
) {
  fun getUserCaseList(
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
    teamCode: String? = null,
    peopleType: PeopleType? = null,
  ): ApiResponseDto<List<CaseDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val entries = userCaseListEntryRepository.findAllBySasUserIdAndListType(user.id, CaseListType.CUSTOM)
    if (entries.isEmpty()) return toApiResponseDto(data = emptyList())

    val caseEntities = caseRepository.findAllWithIdentifiersByIdIn(entries.map { it.sasCaseId })
    val result = toCaseDtos(user.username, caseEntities)
    return toApiResponseDto(
      data = result.data
        .filterCases(
          searchTerm = searchTerm,
          riskLevel = riskLevel,
          teamCode = teamCode,
          username = null,
          peopleType = peopleType,
        )
        .sortCases(today = LocalDate.now(clock)),
      upstreamFailures = result.upstreamFailures,
    )
  }

  fun getSearchHistoryCaseList(): ApiResponseDto<List<CaseDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val entries = userCaseListEntryRepository.findAllBySasUserIdAndListType(user.id, CaseListType.SEARCH_HISTORY)
    if (entries.isEmpty()) return toApiResponseDto(data = emptyList())

    val lastUpdatedAtByCaseId = entries.associate { it.sasCaseId to it.lastUpdatedAt }
    val caseEntities = caseRepository.findAllWithIdentifiersByIdIn(entries.map { it.sasCaseId })
      .sortedWith(compareByDescending<CaseEntity> { lastUpdatedAtByCaseId[it.id] }.thenBy { it.latestCrn() })
    val result = toCaseDtos(user.username, caseEntities)
    return toApiResponseDto(
      data = result.data.obfuscateLimitedAccessCases(),
      upstreamFailures = result.upstreamFailures,
    )
  }

  private fun toCaseDtos(username: String, caseEntities: List<CaseEntity>): OrchestrationResultDto<List<CaseDto>> {
    val crns = caseEntities.map { it.latestCrn() }
    val orchestrationResult = userCaseListOrchestrationService.getCaseAccessAndSummaries(username, crns)
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
    }
    return OrchestrationResultDto(data = caseDtos, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  fun getUserCaseListCrns(listType: CaseListType): ApiResponseDto<List<String>> {
    val user = userService.authorizeAndRetrieveUser()
    val entries = userCaseListEntryRepository.findLatestCrnsBySasUserIdAndListType(user.id, listType)
    val sortedEntries = when (listType) {
      CaseListType.CUSTOM -> entries.sortedBy { it.crn }
      CaseListType.SEARCH_HISTORY -> entries.sortedWith(
        compareByDescending<UserCaseListCrn> { it.lastUpdatedAt }.thenBy { it.crn },
      )
    }
    return toApiResponseDto(data = sortedEntries.map { it.crn })
  }
}
