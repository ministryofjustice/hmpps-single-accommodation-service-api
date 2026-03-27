package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.OrchestrationResult

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getData(crn: String): OrchestrationResult<EligibilityOrchestrationDto> {
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
    val cpr = stdResults.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD)
    val tier = stdResults.getResult<Tier>(GET_TIER)
    val cas1Application = stdResults.getResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = stdResults.getResult<Cas3Application>(GET_CAS_3_APPLICATION)

    return OrchestrationResult(
      data = EligibilityOrchestrationDto(
        crn,
        cpr,
        tier,
        cas1Application,
        cas3Application,
      ),
      upstreamFailures = stdResults.getFailures(),
    )
  }

  fun getPrisonerData(prisonerNumbers: List<String>): OrchestrationResult<List<Prisoner>> {
    val calls = prisonerNumbers.associate {
      "$GET_PRISONER$it" to { prisonerSearchCachingService.getPrisoner(it) }
    }

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val stdResults = results.standardCallsNoIterationResults!!

    return OrchestrationResult(
      data = prisonerNumbers.mapNotNull {
        stdResults.getResult<Prisoner>("$GET_PRISONER$it")
      },
      upstreamFailures = stdResults.getFailures(),
    )
  }
}
