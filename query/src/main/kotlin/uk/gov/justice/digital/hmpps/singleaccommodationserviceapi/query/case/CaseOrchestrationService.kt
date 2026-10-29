package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.FULL_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val sasAndDeliusCachingService: SasAndDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
  val limitedAccessService: LimitedAccessService,
  @param:Value($$"${case-list.page-size:100}") val pageSize: Long,
) {

  private val log = LoggerFactory.getLogger(javaClass)
  private val initialPage = 0L

  @Cacheable(FULL_CASE_LIST, unless = "!#result.upstreamFailures.isEmpty()")
  fun getCaseList(username: String, teamCode: String?): OrchestrationResultDto<CaseListOrchestrationDto> {
    log.debug("Retrieving case list from Delius for username {}", username)
    val initialCall = mapOf(
      getCallKey(username, initialPage) to {
        sasAndDeliusCachingService.getCaseList(
          username = username,
          teamCode = teamCode,
          page = initialPage,
          size = pageSize,
        )
      },
    )

    val initialResults = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = initialCall,
    )

    val initialResultSet = initialResults.standardCallsNoIterationResults!!

    val caseList = initialResultSet
      .getResult<CaseList>(getCallKey(username, initialPage))

    val (additionalCases, additionalFailures) =
      caseList?.let { list ->

        log.debug(
          "Received {} cases, page {} of {}",
          list.cases.size,
          list.page.number + 1,
          list.page.totalPages,
        )

        getRemainingCases(list.page, username, teamCode)
      } ?: (emptyList<Case>() to emptyList())

    val cases = caseList?.cases?.plus(additionalCases) ?: emptyList()
    val caseAccessResults = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = limitedAccessService.userAccessCalls(username, cases.map { it.crn }),
    ).standardCallsNoIterationResults!!

    return OrchestrationResultDto(
      data = CaseListOrchestrationDto(cases = cases, caseAccess = limitedAccessService.getCaseAccess(caseAccessResults)),
      upstreamFailures = initialResultSet.getFailures() + additionalFailures + caseAccessResults.getFailures(),
    )
  }

  private fun getCallKey(username: String, page: Long) = GET_CASE_LIST + username + page

  private fun getRemainingCases(page: PageMetadata, username: String, teamCode: String?) = if (page.number + 1 < page.totalPages) {
    val remainingPages = 1 until page.totalPages
    val additionalResults = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = remainingPages.associate { nextPage ->
        (getCallKey(username, nextPage)) to {
          sasAndDeliusCachingService.getCaseList(
            username,
            teamCode = teamCode,
            page = nextPage,
            size = pageSize,
          )
        }
      },
    )

    val resultSet = additionalResults.standardCallsNoIterationResults!!

    val cases = remainingPages.mapNotNull { nextPage ->
      resultSet.getResult<CaseList>(getCallKey(username, nextPage))?.cases
    }.flatten()

    cases to resultSet.getFailures()
  } else {
    null
  }

  fun getCase(username: String, crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(username, crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      ApiCallKeys.GET_TIER_V3 to { tierCachingService.getTier(crn) },
      ApiCallKeys.GET_SEXUAL_OFFENCE_REGISTRATIONS to { sasAndDeliusCachingService.getSexualOffenceRegistrations(crn) },
    ) + limitedAccessService.userAccessCalls(username, listOf(crn))
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val case = results.standardCallsNoIterationResults!!.getResult<Case>(ApiCallKeys.GET_CASE)
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER_V3)
    val sexualOffenceRegistrations = results.standardCallsNoIterationResults!!.getResult<SexualOffenceRegistrations>(ApiCallKeys.GET_SEXUAL_OFFENCE_REGISTRATIONS)

    return OrchestrationResultDto(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        tier = tier,
        case = case,
        sexualOffenceRegistrations = sexualOffenceRegistrations,
        caseAccess = limitedAccessService.getCaseAccess(results.standardCallsNoIterationResults!!),
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }

  fun getCaseFromDelius(username: String, crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(username, crn) },
    ) + limitedAccessService.userAccessCalls(username, listOf(crn))
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val case = results.standardCallsNoIterationResults!!.getResult<Case>(ApiCallKeys.GET_CASE)

    return OrchestrationResultDto(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = null,
        tier = null,
        case = case,
        sexualOffenceRegistrations = null,
        caseAccess = limitedAccessService.getCaseAccess(results.standardCallsNoIterationResults!!),
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
