package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.ProbationIntegrationSasDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseApplicationOrchestrationService(
  val aggregatorService: AggregatorService,
  val tierCachingService: TierCachingService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val probationIntegrationSasDeliusCachingService: ProbationIntegrationSasDeliusCachingService,

) {

  fun getFreshCases(crns: List<String>): List<CaseApplicationOrchestrationDto> {
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_TIER to { crn: String -> tierCachingService.getTier(crn) },
      ApiCallKeys.GET_CAS_1_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas1Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = emptyMap(),
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val tier = calls[ApiCallKeys.GET_TIER] as? Tier
      val cas1Application = calls[ApiCallKeys.GET_CAS_1_APPLICATION] as? Cas1Application

      CaseApplicationOrchestrationDto(
        crn,
        tier,
        cas1Application,
      )
    }
  }

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
}
