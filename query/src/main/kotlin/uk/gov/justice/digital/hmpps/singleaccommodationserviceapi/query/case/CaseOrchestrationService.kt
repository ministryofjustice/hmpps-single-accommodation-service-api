package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.fetchAllPages
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

  @Cacheable(FULL_CASE_LIST, unless = "!#result.upstreamFailures.isEmpty()")
  fun getCaseList(
    viewerUsername: String,
    caseloadUsername: String,
    teamCode: String?,
  ): OrchestrationResultDto<CaseListOrchestrationDto> {
    log.debug("Retrieving case list from Delius for username {}", caseloadUsername)
    val caseList = aggregatorService.fetchAllPages(
      callKeyForPage = { page -> GET_CASE_LIST + caseloadUsername + page },
      fetchPage = { page ->
        sasAndDeliusCachingService.getCaseList(
          username = caseloadUsername,
          teamCode = teamCode,
          page = page,
          size = pageSize,
        )
      },
      pageMetadata = CaseList::page,
      content = CaseList::cases,
    )
    val cases = caseList.data
    log.debug("Received {} cases", cases.size)

    val caseAccessResults = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = limitedAccessService.userAccessCalls(viewerUsername, cases.map { it.crn }),
    ).standardCallsNoIterationResults!!

    return OrchestrationResultDto(
      data = CaseListOrchestrationDto(cases = cases, caseAccess = limitedAccessService.getCaseAccess(caseAccessResults)),
      upstreamFailures = caseList.upstreamFailures + caseAccessResults.getFailures(),
    )
  }

  fun getCase(username: String, crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(crn) },
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
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(crn) },
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
