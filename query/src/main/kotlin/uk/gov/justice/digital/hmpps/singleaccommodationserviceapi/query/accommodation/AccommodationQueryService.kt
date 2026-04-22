package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Address
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
) {
  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn)
    val currentAccommodation = orchestrationResult.data.cprAddresses?.addresses?.let {
      getCurrentAccommodation(crn, addresses = it)
    }
    return toApiResponseDto(
      data = currentAccommodation,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCurrentAccommodation(crn: String, addresses: List<Address>): AccommodationSummaryDto? = addresses
    .firstOrNull { it.addressStatus == AddressStatus.M }
    ?.let { toAccommodationSummary(crn, address = it) }

  fun getAccommodationHistory(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordByCrn(crn)
    val corePersonRecord = orchestrationResult.data
    return generateAccommodationHistoryResponse(
      crn,
      addresses = corePersonRecord.cpr?.addresses,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getAccommodationHistoryV2(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn)
    val corePersonRecordAddresses = orchestrationResult.data
    return generateAccommodationHistoryResponse(
      crn,
      corePersonRecordAddresses.cprAddresses?.addresses,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  private fun generateAccommodationHistoryResponse(
    crn: String,
    addresses: List<Address>?,
    upstreamFailures: List<UpstreamFailure>,
  ): ApiResponseDto<List<AccommodationSummaryDto>> {
    val accommodationHistory = addresses?.map { toAccommodationSummary(crn, address = it) } ?: emptyList()
    return toApiResponseDto(
      data = accommodationHistory,
      upstreamFailures = upstreamFailures,
    )
  }
}
