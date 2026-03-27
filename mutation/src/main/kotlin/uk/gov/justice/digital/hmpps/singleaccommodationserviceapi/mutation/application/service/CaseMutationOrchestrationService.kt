package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.model.CaseOrchestrationDto

@Service
class CaseMutationOrchestrationService(
  val aggregatorService: AggregatorService,
  val tierCachingService: TierCachingService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  fun getCase(crn: String): CaseOrchestrationDto {
    val calls = mapOf(
      GET_TIER to { tierCachingService.getTier(crn) },
      GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val tier = results.standardCallsNoIterationResults!![GET_TIER] as? Tier
      ?: error("$GET_TIER failed for $crn")
    val cas1Application = results.standardCallsNoIterationResults!![GET_CAS_1_APPLICATION] as? Cas1Application
    return CaseOrchestrationDto(crn, tier, cas1Application)
  }
}
