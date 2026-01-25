package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.String

fun buildAccommodationDetail(
  id: UUID = UUID.randomUUID(),
  name: String? = null,
  crn: String = "X12345",
  arrangementType: AccommodationArrangementType = AccommodationArrangementType.PRIVATE,
  arrangementSubType: AccommodationArrangementSubType? = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
  arrangementSubTypeDescription: String? = null,
  settledType: AccommodationSettledType = AccommodationSettledType.TRANSIENT,
  offenderReleaseType: OffenderReleaseType? = null,
  verificationStatus: VerificationStatus? = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus? = NextAccommodationStatus.YES,
  address: AccommodationAddressDetails = buildAccommodationAddressDetails(),
  startDate: LocalDate? = null,
  endDate: LocalDate? = null,
  createdBy: String = "Joe Bloggs",
  createdAt: Instant = Instant.now(),
) = AccommodationDetail(
  id = id,
  crn = crn,
  name = name,
  arrangementType = arrangementType,
  arrangementSubType = arrangementSubType,
  arrangementSubTypeDescription = arrangementSubTypeDescription,
  settledType = settledType,
  offenderReleaseType = offenderReleaseType,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  address = address,
  startDate = startDate,
  endDate = endDate,
  createdBy = createdBy,
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
