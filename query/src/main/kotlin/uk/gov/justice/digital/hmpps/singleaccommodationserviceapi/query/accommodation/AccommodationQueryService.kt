package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
) {
  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordByCrn(crn)
    val currentAccommodation = orchestrationResult.data.cpr?.addresses?.let {
      getCurrentAccommodation(crn, addresses = it)
    }
    return toApiResponseDto(
      data = currentAccommodation,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCurrentAccommodation(crn: String, addresses: List<CanonicalAddress>): AccommodationSummaryDto? = addresses
    .firstOrNull { it.status.code == AddressStatusCode.M.name }
    ?.let { toAccommodationSummary(crn, address = it) }

  // TODO implement this once we understand how next accommodation is calculated - will also contain cas1 and cas3 arguments
  fun getNextAccommodation(crn: String, addresses: List<CanonicalAddress>): AccommodationSummaryDto? = null

  fun getAccommodationHistory(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordByCrn(crn)
    val corePersonRecord = orchestrationResult.data
    val nonProposedAddresses = corePersonRecord.cpr?.addresses
      ?.filter {
        it.status.code != AddressStatusCode.PR.name &&
          it.status.code != AddressStatusCode.PR1.name
      }
    return generateAccommodationHistoryResponse(
      crn,
      addresses = nonProposedAddresses,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  private fun generateAccommodationHistoryResponse(
    crn: String,
    addresses: List<CanonicalAddress>?,
    upstreamFailures: List<UpstreamFailure>,
  ): ApiResponseDto<List<AccommodationSummaryDto>> {
    val accommodationHistory = addresses?.map { toAccommodationSummary(crn, address = it) } ?: emptyList()
    return toApiResponseDto(
      data = accommodationHistory,
      upstreamFailures = upstreamFailures,
    )
  }
}
