package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Address
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
  private val caseRepository: CaseRepository,
) {
  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    //make extra call ib orchestrator to prisoner search by crn
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn)
    if (orchestrationResult.prisonSearchResult.status == "IN") {
      return toApiResponseDto(
        data = orchestrationResult.prisonSearchResult,
        upstreamFailures = orchestrationResult.upstreamFailures,
      )
    } else {
      orchestrationResult.data.cprAddresses?.addresses?.let {
        getCurrentAccommodation(crn, addresses = it)
      }
      return toApiResponseDto(
        data = currentAccommodation,
        upstreamFailures = orchestrationResult.upstreamFailures,
      )
    }
  }

  fun getCurrentAccommodation(crn: String, addresses: List<Address>): AccommodationSummaryDto? = addresses
    .firstOrNull { it.addressStatus == AddressStatus.M }
    ?.let {
      if (it.addressUsage.addressUsageCode == null) {
        // make a call to prison search
      } else {
        toAccommodationSummary(crn, address = it)
      }
    }

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
    val case = caseRepository.findByCrn(crn)!!
    //make extra call ib orchestrator to prisoner search by crn
    val nomisNumber = case.caseIdentifiers.first { it.identifierType == IdentifierType.PRISON_NUMBER }
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn, nomisNumber!!)
    val corePersonRecordAddresses = orchestrationResult.data
    val list = generateAccommodationHistoryResponse(
      crn,
      corePersonRecordAddresses.cprAddresses?.addresses,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
    if (orchestrationResult.prisonSearchResult.status == "IN") {
      list.add(0, orchestrationResult.prisonSearchResult)
    }
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
