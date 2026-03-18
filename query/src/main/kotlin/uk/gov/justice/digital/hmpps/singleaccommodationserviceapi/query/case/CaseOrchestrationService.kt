package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.OrchestrationResult

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
      ?.get(GET_CASE_LIST) as? CaseList
      ?: CaseList(emptyList())
  }

  fun getCases(crns: List<String>): List<OrchestrationResult<CaseOrchestrationDto>> {
    val bulkCall = mapOf(
      ApiCallKeys.GET_CASE_SUMMARIES to { probationIntegrationDeliusCachingService.getCaseSummaries(crns) },
    )

    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { crn: String ->
        corePersonRecordCachingService.getCorePersonRecordByCrn(
          crn,
        )
      },
      ApiCallKeys.GET_ROSH_DETAIL to { crn: String -> probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { crn: String -> tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    val caseSummaries = results.standardCallsNoIterationResults
      ?.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARIES)
      ?: CaseSummaries(emptyList())

    val bulkFailures = results.standardCallsNoIterationResults?.getFailures() ?: emptyList()

    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cases = caseSummaries.cases.filter { it.crn == crn }

      val cpr = calls.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
      val roshDetails = calls.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
      val tier = calls.getResult<Tier>(ApiCallKeys.GET_TIER)

      val failures = calls.getFailures()

      OrchestrationResult(
        data = CaseOrchestrationDto(
          crn = crn,
          cpr = cpr,
          roshDetails = roshDetails,
          tier = tier,
          cases = cases,
        ),
        upstreamFailures = bulkFailures + failures,
      )
    }
  }

  fun getCase(crn: String): OrchestrationResult<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE_SUMMARY to { probationIntegrationDeliusCachingService.getCaseSummary(crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val stdResults = results.standardCallsNoIterationResults!!
    val caseSummaries = stdResults.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARY)
    val cpr = stdResults.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val roshDetails = stdResults.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = stdResults.getResult<Tier>(ApiCallKeys.GET_TIER)

    val failures = stdResults.getFailures()

    return OrchestrationResult(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        roshDetails = roshDetails,
        tier = tier,
        cases = caseSummaries?.cases,
      ),
      upstreamFailures = failures,
    )
  }
}
