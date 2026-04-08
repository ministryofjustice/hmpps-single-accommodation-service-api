package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
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

  fun getCase(crn: String): CaseMutationOrchestrationDto {
    val calls = mapOf(
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
      ApiCallKeys.GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)
    val cas1Application = results.standardCallsNoIterationResults!!.getResult<Cas1Application>(ApiCallKeys.GET_CAS_1_APPLICATION)
    return CaseMutationOrchestrationDto(crn, cpr = null, tier, cas1Application)
  }

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
      val tier = calls.getResult<Tier>(ApiCallKeys.GET_TIER)
      val cas1Application = calls.getResult<Cas1Application>(ApiCallKeys.GET_CAS_1_APPLICATION)
      val cpr = calls.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)

      CaseMutationOrchestrationDto(
        crn,
        cpr,
        tier,
        cas1Application,
      )
    }
  }
}
