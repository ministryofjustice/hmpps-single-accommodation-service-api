package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.extractFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getOptionalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getRequiredResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.OrchestrationResult

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {

  fun getCases(crns: List<String>): List<OrchestrationResult<CaseOrchestrationDto>> {
    val bulkCall = mapOf(
      ApiCallKeys.GET_CASE_SUMMARIES to { probationIntegrationDeliusCachingService.getCaseSummaries(crns) },
    )

    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD to { crn: String -> corePersonRecordCachingService.getCorePersonRecord(crn) },
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
      ?.getOptionalResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARIES)
      ?: CaseSummaries(emptyList())

    val bulkFailures = results.standardCallsNoIterationResults?.extractFailures() ?: emptyList()

    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cases = caseSummaries.cases.filter { it.crn == crn }

      val cpr = calls.getOptionalResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD)
      val roshDetails = calls.getOptionalResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
      val tier = calls.getOptionalResult<Tier>(ApiCallKeys.GET_TIER)

      val failures = calls.extractFailures()

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
      ApiCallKeys.GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val stdResults = results.standardCallsNoIterationResults!!
    val caseSummaries = stdResults.getRequiredResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARY)
    val cpr = stdResults.getRequiredResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD)
    val roshDetails = stdResults.getRequiredResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = stdResults.getRequiredResult<Tier>(ApiCallKeys.GET_TIER)

    val failures = stdResults.extractFailures()

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
