package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CORE_PERSON_RECORD
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService

@Service
class RulesOrchestrationService(
  val aggregatorService: AggregatorService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getCprAndTier(crn: String): RulesOrchestrationDto {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!![GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("$GET_CORE_PERSON_RECORD failed for $crn")
    val tier = results.standardCallsNoIterationResults!![GET_TIER] as? Tier
      ?: error("$GET_TIER failed for $crn")

    return RulesOrchestrationDto(crn, cpr, tier)
  }

  fun getPrisoner(prisonerNumber: String): Prisoner {
    val calls = mapOf(
      GET_PRISONER to { prisonerSearchCachingService.getPrisoner(prisonerNumber) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    return results.standardCallsNoIterationResults!![GET_PRISONER] as? Prisoner
      ?: error("$GET_PRISONER failed for $prisonerNumber")
  }
}
