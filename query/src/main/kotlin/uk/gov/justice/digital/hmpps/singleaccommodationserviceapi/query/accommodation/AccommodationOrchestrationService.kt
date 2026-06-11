package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
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
  fun getAccommodationOrchestration(crn: String, prisonNumber: String?): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = buildMap {
      put(GET_CORE_PERSON_RECORD_BY_CRN) { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) }
      put(GET_CAS_1_CURRENT_PREMISES) { approvedPremisesCachingService.getCas1CurrentPremises(crn) }
      prisonNumber?.let { num -> put(GET_PRISONER) { prisonerSearchCachingService.getPrisoner(num) } }
    }
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    val prisoner = results.getResult<Prisoner>(GET_PRISONER)
    val cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val cas1CurrentPremises = results.getResult<Cas1PremisesSummary>(GET_CAS_1_CURRENT_PREMISES)

    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr = cpr,
        cas1Application = null,
        cas3Application = null,
        prisoner = prisoner,
        cas1CurrentPremises = cas1CurrentPremises,
      ),
      upstreamFailures = results.getFailures(),
    )
  }

  fun getNextAccommodationOrchestration(crn: String, prisonNumber: String?): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = buildMap {
      put(GET_CORE_PERSON_RECORD_BY_CRN) { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) }
      put(GET_CAS_1_CURRENT_PREMISES) { approvedPremisesCachingService.getCas1CurrentPremises(crn) }
      put(GET_CAS_1_APPLICATION) { approvedPremisesCachingService.getSuitableCas1Application(crn) }
      put(GET_CAS_3_APPLICATION) { approvedPremisesCachingService.getSuitableCas3Application(crn) }
      prisonNumber?.let { num -> put(GET_PRISONER) { prisonerSearchCachingService.getPrisoner(num) } }
    }
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    val prisoner = results.getResult<Prisoner>(GET_PRISONER)
    val cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val cas1Application = results.getResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = results.getResult<Cas3Application>(GET_CAS_3_APPLICATION)
    val cas1CurrentPremises = results.getResult<Cas1PremisesSummary>(GET_CAS_1_CURRENT_PREMISES)

    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr = cpr,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        prisoner = prisoner,
        cas1CurrentPremises = cas1CurrentPremises,
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
