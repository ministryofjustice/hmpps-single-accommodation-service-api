package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationTransformer.toAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationTransformer.toAccommodationSummary
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
  private val excludedAddressStatuses = setOf(AddressStatusCode.PR.name, AddressStatusCode.PR1.name)

  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val caseEntity = caseRepository.findByCrn(crn)
    val prisonNumber = caseEntity?.latestPrisonNumber()
    val orchestrationResult = accommodationOrchestrationService.getAccommodationsOrchestration(crn, prisonNumber)
    return if (orchestrationResult.upstreamFailures.isNotEmpty()) {
      toApiResponseDto(
        data = null,
        upstreamFailures = orchestrationResult.upstreamFailures,
      )
    } else {
      val currentAccommodation = getCurrentAccommodation(crn = crn, addresses = orchestrationResult.data.cpr?.addresses, prisoner = orchestrationResult.data.prisoner)
      toApiResponseDto(
        data = currentAccommodation,
      )
    }
  }

  fun getNextAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val orchestrationResult = accommodationOrchestrationService.getNextAccommodationData(crn)
    val currentAccommodation = getCurrentAccommodation(crn = crn, addresses = orchestrationResult.data.cpr?.addresses, prisoner = orchestrationResult.data.prisoner)
    val nextAccommodations = getNextAccommodations(
      crn,
      addresses = orchestrationResult.data.cpr?.addresses,
      cas1Application = orchestrationResult.data.cas1Application,
      cas3Application = orchestrationResult.data.cas3Application,
      currentAccommodation = currentAccommodation,
    )

    return toApiResponseDto(
      data = nextAccommodations.firstOrNull(),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCurrentAccommodation(crn: String, addresses: List<CanonicalAddress>?, prisoner: Prisoner?): AccommodationSummaryDto? = if (prisoner?.inOutStatus == InOutStatus.IN) {
    toAccommodationSummary(crn, prisoner)
  } else {
    addresses
      ?.firstOrNull { it.status.code == AddressStatusCode.M.name }
      ?.let { toAccommodationSummary(crn, address = it) }
  }

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

  fun getCurrentAndAllAccommodations(crn: String): ApiResponseDto<Pair<AccommodationSummaryDto?, List<AccommodationDetailDto>>> {
    val caseEntity = caseRepository.findByCrn(crn)
    val prisonNumber = caseEntity?.latestPrisonNumber()
    val orchestrationResult = accommodationOrchestrationService.getAccommodationsOrchestration(crn, prisonNumber)

    val allAccommodations = orchestrationResult.data.cpr?.let {
      it.addresses.map { toAccommodationDetail(crn, address = it) }
    } ?: emptyList()

    val currentAccommodation = getCurrentAccommodation(
      crn = crn,
      addresses = orchestrationResult.data.cpr?.addresses,
      prisoner = orchestrationResult.data.prisoner,
    )

    return toApiResponseDto(
      data = Pair(currentAccommodation, allAccommodations),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getAccommodationHistory(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val caseEntity = caseRepository.findByCrn(crn)
    val prisonNumber = caseEntity?.latestPrisonNumber()

    val orchestrationResult =
      accommodationOrchestrationService.getAccommodationsOrchestration(crn, prisonNumber)

    val data = orchestrationResult.data
    val prisoner = data.prisoner

    val prisonAddress = prisoner
      ?.takeIf { it.inOutStatus == InOutStatus.IN }
      ?.let { toAccommodationSummary(crn, prisoner = it) }

    val notProposedAddresses = data.cpr?.addresses?.filter { it.status.code !in excludedAddressStatuses }

    val accommodationHistory = listOfNotNull(prisonAddress) +
      notProposedAddresses?.map { toAccommodationSummary(crn, address = it) }.orEmpty()

    return toApiResponseDto(
      data = accommodationHistory,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getAccommodation(id: UUID): AccommodationDetailDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findWithIdentifiersById(proposedAccommodationEntity.caseId).orThrowNotFound("id" to proposedAccommodationEntity.id)
    val accommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)
      .orThrowNotFound("id" to proposedAccommodationEntity.accommodationTypeId)
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId?.let {
      accommodationStatusRepository.findByIdOrNull(it).orThrowNotFound("id" to it)
    }
    return toAccommodationDetail(
      crn = case.latestCrn(),
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
    )
  }
}
