package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
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

  fun getCases(crns: List<String>): List<CaseOrchestrationDto> {
    val aggregatorResult = orchestrateGetCasesCalls(crns)
    val caseSummaries = aggregatorResult.standardCallsNoIterationResults
      ?.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARIES)
      ?: CaseSummaries(emptyList())

    return aggregatorResult.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cases = caseSummaries.cases.filter { it.crn == crn }
      val cpr = calls.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
      val roshDetails = calls.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
      val tier = calls.getResult<Tier>(ApiCallKeys.GET_TIER)

      CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        roshDetails = roshDetails,
        tier = tier,
        cases = cases,
      )
    }
  }

  fun getCase(crn: String): CaseOrchestrationDto {
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
      ?: error("${ApiCallKeys.GET_CASE_SUMMARY} failed for $crn")
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
      ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN} failed for $crn")
    val roshDetails = results.standardCallsNoIterationResults!!.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
      ?: error("${ApiCallKeys.GET_ROSH_DETAIL} failed for $crn")
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)
      ?: error("${ApiCallKeys.GET_TIER} failed for $crn")

    return CaseOrchestrationDto(
      crn = crn,
      cpr = cpr,
      roshDetails = roshDetails,
      tier = tier,
      cases = caseSummaries.cases,
    )
  }

  fun getCasesV2(crns: List<String>): OrchestrationResultDto<List<CaseOrchestrationDto>> {
    val aggregatorResult = orchestrateGetCasesCalls(crns)
    val caseSummaries = aggregatorResult.standardCallsNoIterationResults!!.getResult<CaseSummaries>(ApiCallKeys.GET_CASE_SUMMARIES)
      ?: CaseSummaries(emptyList())
    val bulkFailures = aggregatorResult.standardCallsNoIterationResults!!.getFailures()

    val allPerIdentifierFailures = mutableListOf<UpstreamFailure>()
    val data = aggregatorResult.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cases = caseSummaries.cases.filter { it.crn == crn }
      val cpr = calls.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
      val roshDetails = calls.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
      val tier = calls.getResult<Tier>(ApiCallKeys.GET_TIER)
      allPerIdentifierFailures.addAll(calls.getFailures())

      CaseOrchestrationDto(crn = crn, cpr = cpr, roshDetails = roshDetails, tier = tier, cases = cases)
    }

    return OrchestrationResultDto(
      data = data,
      upstreamFailures = bulkFailures + allPerIdentifierFailures,
    )
  }

  fun getCaseV2(crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
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

  private fun orchestrateGetCasesCalls(crns: List<String>): AggregatorResult {
    val bulkCall = mapOf(
      ApiCallKeys.GET_CASE_SUMMARIES to { probationIntegrationDeliusCachingService.getCaseSummaries(crns) },
    )
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { crn: String ->
        corePersonRecordCachingService.getCorePersonRecordByCrn(crn)
      },
    )
    return aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
  }
}
