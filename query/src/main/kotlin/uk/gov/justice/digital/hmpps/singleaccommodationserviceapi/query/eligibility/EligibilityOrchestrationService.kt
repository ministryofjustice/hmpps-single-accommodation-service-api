package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_2_COURT_BAIL_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_2_HDC_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_2_PRISON_BAIL_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

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
      GET_CAS_2_HDC_APPLICATION to { approvedPremisesCachingService.getSuitableCas2HdcApplication(crn) },
      GET_CAS_2_PRISON_BAIL_APPLICATION to { approvedPremisesCachingService.getSuitableCas2PrisonBailApplication(crn) },
      GET_CAS_2_COURT_BAIL_APPLICATION to { approvedPremisesCachingService.getSuitableCas2CourtBailApplication(crn) },
      )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!![GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("$GET_CORE_PERSON_RECORD failed for $crn")
    val tier = results.standardCallsNoIterationResults!![GET_TIER] as? Tier
      ?: error("$GET_TIER failed for $crn")
    val cas1Application = results.standardCallsNoIterationResults!![GET_CAS_1_APPLICATION] as? Cas1Application
    val cas2HdcApplication = results.standardCallsNoIterationResults!![GET_CAS_2_HDC_APPLICATION] as? Cas2HdcApplication
    val cas2PrisonBailApplication = results.standardCallsNoIterationResults!![GET_CAS_2_PRISON_BAIL_APPLICATION] as? Cas2PrisonBailApplication
    val cas2CourtBailApplication = results.standardCallsNoIterationResults!![GET_CAS_2_COURT_BAIL_APPLICATION] as? Cas2CourtBailApplication

    return EligibilityOrchestrationDto(
      crn,
      cpr,
      tier,
      cas1Application,
      cas2HdcApplication,
      cas2PrisonBailApplication,
      cas2CourtBailApplication,
      )
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
