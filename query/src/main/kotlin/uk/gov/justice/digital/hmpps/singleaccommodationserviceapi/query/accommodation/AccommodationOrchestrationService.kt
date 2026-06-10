package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService

@Service
class AccommodationOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
  private val prisonerSearchCachingService: PrisonerSearchCachingService,
) {
  fun getAccommodationsOrchestration(crn: String) = getAccommodationsOrchestration(crn, prisonNumber = null)

  fun getAccommodationsOrchestration(crn: String, prisonNumber: String?): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = buildMap {
      put(GET_CORE_PERSON_RECORD_BY_CRN) { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) }
      prisonNumber?.let { num -> put(GET_PRISONER) { prisonerSearchCachingService.getPrisoner(num) } }
    }
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    val prisoner = results.getResult<Prisoner>(GET_PRISONER)
    val cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr = cpr,
        cas1Application = null,
        cas3Application = null,
        prisoner = prisoner,
      ),
      upstreamFailures = results.getFailures(),
    )
  }

  fun getNextAccommodationData(crn: String): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
      GET_CAS_3_APPLICATION to { approvedPremisesCachingService.getSuitableCas3Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val cas1Application = results.standardCallsNoIterationResults!!.getResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = results.standardCallsNoIterationResults!!.getResult<Cas3Application>(GET_CAS_3_APPLICATION)

    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr,
        cas1Application,
        cas3Application,
        prisoner = null,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
