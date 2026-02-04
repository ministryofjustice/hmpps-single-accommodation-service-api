package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.String

fun buildAccommodationDetail(
  id: UUID = UUID.randomUUID(),
  name: String? = null,
  arrangementType: AccommodationArrangementType = AccommodationArrangementType.PRIVATE,
  arrangementSubType: AccommodationArrangementSubType? = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
  arrangementSubTypeDescription: String? = null,
  settledType: AccommodationSettledType = AccommodationSettledType.TRANSIENT,
  offenderReleaseType: OffenderReleaseType? = null,
  status: AccommodationStatus? = AccommodationStatus.NOT_CHECKED_YET,
  address: AccommodationAddressDetails = buildAccommodationAddressDetails(),
  startDate: LocalDate? = null,
  endDate: LocalDate? = null,
  createdAt: Instant = Instant.now(),
) = AccommodationDetail(
  id = id,
  name = name,
  arrangementType = arrangementType,
  arrangementSubType = arrangementSubType,
  arrangementSubTypeDescription = arrangementSubTypeDescription,
  settledType = settledType,
  offenderReleaseType = offenderReleaseType,
  status = status,
  address = address,
  startDate = startDate,
  endDate = endDate,
  createdAt = createdAt,
)

fun buildAccommodationAddressDetails(
  postcode: String = "SW1A 1AA",
  subBuildingName: String? = "The Sub-Building",
  buildingName: String? = "The Building",
  buildingNumber: String? = "123",
  thoroughfareName: String? = "The Road",
  dependentLocality: String? = "The Area",
  postTown: String = "London",
  county: String? = "London",
  country: String = "England",
  uprn: String? = "1234567890",
) = AccommodationAddressDetails(
  postcode = postcode,
  subBuildingName = subBuildingName,
  buildingName = buildingName,
  buildingNumber = buildingNumber,
  thoroughfareName = thoroughfareName,
  dependentLocality = dependentLocality,
  postTown = postTown,
  county = county,
  country = country,
  uprn = uprn,
)
