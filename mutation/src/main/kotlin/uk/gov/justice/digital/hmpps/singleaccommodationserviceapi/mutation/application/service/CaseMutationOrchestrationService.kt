package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseMutationOrchestrationService(
  val aggregatorService: AggregatorService,
  val tierCachingService: TierCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {

  fun getCases(crns: List<String>): List<CaseMutationOrchestrationDto> {
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { crn: String -> corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
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
      val cpr = calls[ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN] as? CorePersonRecord

      CaseMutationOrchestrationDto(
        crn,
        cpr,
        tier,
        cas1Application,
      )
    }
  }
}
