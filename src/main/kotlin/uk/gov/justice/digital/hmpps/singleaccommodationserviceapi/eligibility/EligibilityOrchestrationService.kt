package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CORE_PERSON_RECORD
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityOrchestrationDto

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getData(crn: String): EligibilityOrchestrationDto {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      GET_TIER to { tierCachingService.getTier(crn) },
      GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!![GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("$GET_CORE_PERSON_RECORD failed for $crn")
    val tier = results.standardCallsNoIterationResults!![GET_TIER] as? Tier
      ?: error("$GET_TIER failed for $crn")
    val cas1Application = results.standardCallsNoIterationResults!![GET_CAS_1_APPLICATION] as? Cas1Application

    return EligibilityOrchestrationDto(crn, cpr, tier, cas1Application)
  }

  fun getPrisonerData(prisonerNumbers: List<String>): List<Prisoner> {
    val calls = prisonerNumbers.associate {
      "$GET_PRISONER$it" to { prisonerSearchCachingService.getPrisoner(it) }
    }

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    return prisonerNumbers.mapNotNull {
      results.standardCallsNoIterationResults!!["$GET_PRISONER$it"] as? Prisoner
    }
  }
}
