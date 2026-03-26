package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.extractFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getOptionalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getRequiredResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
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
      GET_CAS_3_APPLICATION to { approvedPremisesCachingService.getSuitableCas3Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val stdResults = results.standardCallsNoIterationResults!!
    val cpr = stdResults.getRequiredResult<CorePersonRecord>(GET_CORE_PERSON_RECORD)
    val tier = stdResults.getRequiredResult<Tier>(GET_TIER)
    val cas1Application = stdResults.getOptionalResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = stdResults.getOptionalResult<Cas3Application>(GET_CAS_3_APPLICATION)

    return EligibilityOrchestrationDto(
      crn,
      cpr,
      tier,
      cas1Application,
      cas3Application,
      upstreamFailures = stdResults.extractFailures(),
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
      results.standardCallsNoIterationResults!!.getOptionalResult<Prisoner>("$GET_PRISONER$it")
    }
  }
}
