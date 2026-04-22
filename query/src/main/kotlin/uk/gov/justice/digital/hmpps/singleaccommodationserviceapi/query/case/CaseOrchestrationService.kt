package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.ProbationIntegrationSasDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val probationIntegrationSasDeliusCachingService: ProbationIntegrationSasDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {
  fun getCaseList(username: String): CaseList {
    val bulkCall = mapOf(
      GET_CASE_LIST to { probationIntegrationSasDeliusCachingService.getCaseList(username) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
    )
    return results.standardCallsNoIterationResults
      ?.getResult<CaseList>(GET_CASE_LIST)
      ?: CaseList(emptyList())
  }

  fun getCase(crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE_SUMMARY to { probationIntegrationDeliusCachingService.getCaseSummary(crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val caseSummaries = results.standardCallsNoIterationResults!!.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARY)
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val roshDetails = results.standardCallsNoIterationResults!!.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)

    return OrchestrationResultDto(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        roshDetails = roshDetails,
        tier = tier,
        cases = caseSummaries?.cases,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
