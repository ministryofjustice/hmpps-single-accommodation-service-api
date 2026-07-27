package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import java.time.Instant
import java.util.UUID

fun buildProposedAccommodationDto(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  accommodationType: AccommodationTypeDto = buildAccommodationTypeDto(),
  verificationStatus: VerificationStatus? = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus? = NextAccommodationStatus.YES,
  address: AccommodationAddressDetails = buildAccommodationAddressDetails(),
  createdBy: String = "Joe Bloggs",
  createdAt: Instant = Instant.now(),
) = ProposedAccommodationDto(
  id = id,
  crn = crn,
  accommodationType = accommodationType,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  address = address,
  createdBy = createdBy,
  createdAt = createdAt,
)

fun buildAccommodationAddressDetails(
  postcode: String? = "SW1A 1AA",
  subBuildingName: String? = "The Sub-Building",
  buildingName: String? = "The Building",
  buildingNumber: String? = "123",
  thoroughfareName: String? = "The Road",
  dependentLocality: String? = "The Area",
  postTown: String? = "London",
  county: String? = "London",
  country: String? = "England",
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
