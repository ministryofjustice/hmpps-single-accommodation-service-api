package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService

@Service
class AccommodationOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
) {
  fun getCorePersonRecordByCrn(crn: String): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr = cpr,
        cprAddresses = null,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }

  fun getCorePersonRecordAddressesByCrn(crn: String): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordAddressesByCrn(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val cprAddresses = results.standardCallsNoIterationResults!!.getResult<CorePersonRecordAddresses>(GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN)
    return OrchestrationResultDto(
      data = AccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = cprAddresses,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
