package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import java.util.UUID

@Service
class AccommodationSummaryCalculator(
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
) {
  private val log = LoggerFactory.getLogger(javaClass)
  private val proposedAccommodationStatuses = setOf(AddressStatusCode.PR.name, AddressStatusCode.PR1.name)

  private val transientAccommodationTypeCodes: Set<String> by lazy {
    accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(
      AccommodationSettledType.TRANSIENT,
    ).map { it.code }.toSet()
  }
  private val settledAccommodationTypeCodes: Set<String> by lazy {
    accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(
      AccommodationSettledType.SETTLED,
    ).map { it.code }.toSet()
  }
  private val homelessAccommodationTypeCodes: Set<String> by lazy {
    accommodationTypeRepository.findAllByIsHomelessIsTrueAndActiveIsTrue().map { it.code }.toSet()
  }
  private fun typeExists(code: String?): Boolean =
    if (code == null) {
      false
    } else {
      try {
        accommodationTypeRepository.findByCode(code) != null
      } catch (e: Exception) {
        log.warn("Unable to look up accommodation type for code {}", code, e)
        false
      }
    }

  fun calculateAccommodationSummaries(
    crn: String,
    addresses: List<CanonicalAddress>?,
    prisoner: Prisoner?,
    cas1CurrentPremises: Cas1PremisesSummary?,
    cas3CurrentPremises: Cas3PremisesSummary?,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
  ): AccommodationSummariesDto {
    val currentAccommodation = calculateCurrentAccommodation(
      crn = crn,
      addresses = addresses,
      prisoner = prisoner,
      cas1CurrentPremises = cas1CurrentPremises,
      cas3CurrentPremises = cas3CurrentPremises,
    )

    val nextAccommodation = calculateNextAccommodations(
      crn = crn,
      addresses = addresses,
      cas1Application = cas1Application,
      cas3Application = cas3Application,
      currentAccommodation = currentAccommodation,
    ).firstOrNull()

    return AccommodationSummariesDto(
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      caseAccommodationStatus = calculateCaseAccommodationStatus(currentAccommodation, nextAccommodation),
    )
  }

  fun calculateCurrentAccommodation(
    crn: String,
    addresses: List<CanonicalAddress>?,
    prisoner: Prisoner?,
    cas1CurrentPremises: Cas1PremisesSummary?,
    cas3CurrentPremises: Cas3PremisesSummary?,
  ): AccommodationSummaryDto? = if (prisoner?.inOutStatus == InOutStatus.IN) {
    toAccommodationSummary(crn, prisoner, includePrisonNameInAddress = true)
  } else {
    addresses
      ?.firstOrNull { it.status.code == AddressStatusCode.M.name }?.let { mainAddress ->
        when {
          isAddressWithUsageCode(mainAddress, AddressUsageCode.A02) && postcodesMatch(cas1CurrentPremises?.postcode, mainAddress.postcode) ->
            toAccommodationSummary(
              crn,
              startDate = cas1CurrentPremises?.startDate,
              endDate = cas1CurrentPremises?.endDate,
              address = mainAddress,
            )

          isAddressWithUsageCode(mainAddress, AddressUsageCode.A17) && postcodesMatch(cas3CurrentPremises?.postcode, mainAddress.postcode) ->
            toAccommodationSummary(
              crn,
              startDate = cas3CurrentPremises?.startDate,
              endDate = cas3CurrentPremises?.endDate,
              address = mainAddress,
            )

          else ->
            toAccommodationSummary(
              crn,
              address = mainAddress,
            )
        }
      }
  }

  fun calculateNextAccommodations(
    crn: String,
    addresses: List<CanonicalAddress>?,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
    currentAccommodation: AccommodationSummaryDto?,
  ): List<AccommodationSummaryDto> {
    val cas1NextAccommodation = cas1Application?.placement
      ?.takeIf { it.status == Cas1PlacementStatus.UPCOMING }
      ?.premises?.let {
        toAccommodationSummary(crn, premises = it, currentAccommodation)
      }

    val cas3NextAccommodation = cas3Application?.takeIf { it.bookingStatus == Cas3BookingStatus.CONFIRMED }
      ?.premises?.let {
        toAccommodationSummary(crn, premises = it, currentAccommodation)
      }

    val nextApprovedPremisesAccommodations = listOfNotNull(cas1NextAccommodation, cas3NextAccommodation)

    val nextCprProposedAccommodations = calculateNextCprProposedAccommodations(crn, addresses)

    return (nextApprovedPremisesAccommodations + nextCprProposedAccommodations)
  }

  private fun calculateNextCprProposedAccommodations(
    crn: String,
    addresses: List<CanonicalAddress>?,
  ): List<AccommodationSummaryDto> = addresses
    .orEmpty()
    .filter { it.status.code in proposedAccommodationStatuses }
    .filter { !it.postcode.isNullOrBlank() }
    .filter { it.endDate == null }
    .map { address ->
      val proposedAccommodationId = address.cprAddressId
        ?.let { proposedAccommodationRepository.findByCprAddressId(UUID.fromString(it)) }
        ?.id
      toAccommodationSummary(crn, address = address, maskDates = true, proposedAccommodationId = proposedAccommodationId)
    }

  fun calculateCaseAccommodationStatus(
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
  ): CaseAccommodationStatus? = when {
    isNoFixedAbode(currentAccommodation, nextAccommodation) -> CaseAccommodationStatus.NO_FIXED_ABODE
    isRiskOfNoFixedAbode(currentAccommodation, nextAccommodation) -> CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE
    isSettled(currentAccommodation, nextAccommodation) -> CaseAccommodationStatus.SETTLED
    isTransient(currentAccommodation, nextAccommodation) -> CaseAccommodationStatus.TRANSIENT
    else -> null
  }

  private fun postcodesMatch(postcode1: String?, postcode2: String?) = postcode1?.filterNot(Char::isWhitespace).equals(postcode2?.filterNot(Char::isWhitespace), ignoreCase = true)

  private fun isAddressWithUsageCode(address: CanonicalAddress, usageCode: AddressUsageCode): Boolean = address.usages.find { it.usageCode.code == usageCode.name && it.isActive } != null

  private fun isNoFixedAbode(currentAccommodation: AccommodationSummaryDto?, nextAccommodation: AccommodationSummaryDto?) = (
    isHomelessOrNull(currentAccommodation) &&
      isHomelessOrNull(nextAccommodation)
    )

  private fun isRiskOfNoFixedAbode(
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
  ) = (isSettledType(currentAccommodation)  && isHomelessOrNull(nextAccommodation)) ||
    (isTransientType(currentAccommodation) && isHomelessOrNull(nextAccommodation))

  private fun isSettled(
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
  ) = (isSettledType(currentAccommodation) && currentAccommodation?.endDate == null && nextAccommodation==null) || isSettledType(nextAccommodation)

  private fun isTemporaryTransient(dto: AccommodationSummaryDto?) = isTransientType(dto) && dto?.endDate != null
  private fun isTransient(currentDto: AccommodationSummaryDto?, nextDto: AccommodationSummaryDto?) = isTransientType(currentDto) || isTransientType(nextDto)
  private fun isHomelessOrNull(dto: AccommodationSummaryDto?) = dto == null || isHomelessType(dto) || !typeExists(dto.type?.code)

  private fun isSettledType(dto: AccommodationSummaryDto?) = dto?.type?.code in settledAccommodationTypeCodes
  private fun isTransientType(dto: AccommodationSummaryDto?) = dto?.type?.code in transientAccommodationTypeCodes
  private fun isHomelessType(dto: AccommodationSummaryDto?) = dto?.type?.code in homelessAccommodationTypeCodes
}
