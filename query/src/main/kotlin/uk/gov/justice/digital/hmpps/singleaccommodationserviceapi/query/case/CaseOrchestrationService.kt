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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.OrchestrationResultDto

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val probationIntegrationSasDeliusCachingService: ProbationIntegrationSasDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {
  fun getCaseList(username: String): OrchestrationResultDto<CaseList> {
    val bulkCall = mapOf(
      GET_CASE_LIST to { probationIntegrationSasDeliusCachingService.getCaseList(username) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
    )

    val stdResults = results.standardCallsNoIterationResults!!
    val caseList = stdResults.getResult<CaseList>(GET_CASE_LIST)
      ?: CaseList(emptyList())

    val failures = stdResults.getFailures()

    return OrchestrationResultDto(
      data = caseList,
      upstreamFailures = failures,
    )
  }

  fun getCases(crns: List<String>): List<OrchestrationResultDto<CaseOrchestrationDto>> {
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

      OrchestrationResultDto(
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

    val stdResults = results.standardCallsNoIterationResults!!
    val caseSummaries = stdResults.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARY)
    val cpr = stdResults.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val roshDetails = stdResults.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = stdResults.getResult<Tier>(ApiCallKeys.GET_TIER)

    val failures = stdResults.getFailures()

    return OrchestrationResultDto(
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
