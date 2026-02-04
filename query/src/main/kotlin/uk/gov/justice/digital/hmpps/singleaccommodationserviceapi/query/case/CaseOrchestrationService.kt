package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {

  fun getCases(crns: List<String>): List<CaseOrchestrationDto> {
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
      ?.get(ApiCallKeys.GET_CASE_SUMMARIES) as? CaseSummaries
      ?: CaseSummaries(emptyList())

    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cases = caseSummaries.cases.filter { it.crn == crn }

      val cpr = calls[ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
      val roshDetails = calls[ApiCallKeys.GET_ROSH_DETAIL] as? RoshDetails

      val tier = calls[ApiCallKeys.GET_TIER] as? Tier

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
      ApiCallKeys.GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val caseSummaries = results.standardCallsNoIterationResults!![ApiCallKeys.GET_CASE_SUMMARY] as? CaseSummaries
      ?: error("${ApiCallKeys.GET_CASE_SUMMARY} failed for $crn")
    val cpr = results.standardCallsNoIterationResults!![ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD} failed for $crn")
    val roshDetails = results.standardCallsNoIterationResults!![ApiCallKeys.GET_ROSH_DETAIL] as? RoshDetails
      ?: error("${ApiCallKeys.GET_ROSH_DETAIL} failed for $crn")
    val tier = results.standardCallsNoIterationResults!![ApiCallKeys.GET_TIER] as? Tier
      ?: error("${ApiCallKeys.GET_TIER} failed for $crn")

    return CaseOrchestrationDto(
      crn = crn,
      cpr = cpr,
      roshDetails = roshDetails,
      tier = tier,
      cases = caseSummaries.cases,
    )
  }
}
