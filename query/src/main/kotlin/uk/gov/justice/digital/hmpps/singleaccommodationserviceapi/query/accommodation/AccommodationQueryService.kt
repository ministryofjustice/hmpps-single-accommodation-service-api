package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val caseRepository: CaseRepository,
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

  fun getNextAccommodations(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val orchestrationResult = accommodationOrchestrationService.getNextAccommodationData(crn)
    val currentAccommodation = orchestrationResult.data.cpr?.addresses?.let {
      getCurrentAccommodation(crn, addresses = it)
    }
    val nextAccommodations = getNextAccommodations(
      crn,
      addresses = orchestrationResult.data.cpr?.addresses,
      cas1Application = orchestrationResult.data.cas1Application,
      cas3Application = orchestrationResult.data.cas3Application,
      currentAccommodation = currentAccommodation,
    )

    return toApiResponseDto(
      data = nextAccommodations,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCurrentAccommodation(crn: String, addresses: List<CanonicalAddress>): AccommodationSummaryDto? = addresses
    .firstOrNull { it.status.code == AddressStatusCode.M.name }
    ?.let { toAccommodationSummary(crn, address = it) }

  fun getNextAccommodations(
    crn: String,
    addresses: List<CanonicalAddress>?,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
    currentAccommodation: AccommodationSummaryDto?,
  ): List<AccommodationSummaryDto> {
    val cas1NextAccommodation = cas1Application?.takeIf { it.placementStatus == Cas1PlacementStatus.UPCOMING }
      ?.premises?.let {
        toAccommodationSummary(crn, premises = it, currentAccommodation)
      }

    val cas3NextAccommodation = cas3Application?.takeIf { it.bookingStatus == Cas3BookingStatus.CONFIRMED }
      ?.premises?.let {
        toAccommodationSummary(crn, premises = it, currentAccommodation)
      }

    return (
      (
        addresses
          ?.filter { it.status.code == AddressStatusCode.PR.name || it.status.code == AddressStatusCode.PR1.name }
          ?.map { toAccommodationSummary(crn, address = it) } ?: emptyList()
        ) + cas1NextAccommodation + cas3NextAccommodation
      ).mapNotNull { it }
  }

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

  fun getAccommodation(id: UUID): AccommodationSummaryDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findWithIdentifiersById(proposedAccommodationEntity.caseId).orThrowNotFound("id" to proposedAccommodationEntity.id)
    val accommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)
      .orThrowNotFound("id" to proposedAccommodationEntity.accommodationTypeId)
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId?.let {
      accommodationStatusRepository.findByIdOrNull(it).orThrowNotFound("id" to it)
    }
    return toAccommodationSummary(
      crn = case.latestCrn(),
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
    )
  }
}
