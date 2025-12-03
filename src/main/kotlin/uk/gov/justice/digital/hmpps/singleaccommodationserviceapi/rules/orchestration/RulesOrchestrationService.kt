package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService

@Service
class RulesOrchestrationService(
  val aggregatorService: AggregatorService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
) {

  fun getEligibilityDomainData(crn: String): RulesOrchestrationDto {
    val calls = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!![ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD} failed for $crn")
    val tier = results.standardCallsNoIterationResults!![ApiCallKeys.GET_TIER] as? Tier
      ?: error("${ApiCallKeys.GET_TIER} failed for $crn")
    return RulesOrchestrationDto(crn, cpr, tier)
  }
}
