package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Accommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Address
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Crs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.DutyToRefer
import java.util.UUID

fun buildAccommodation(
  id: UUID = UUID.randomUUID(),
  type: AccommodationType = AccommodationType.PRISON,
  settledType: String = "settled",
  status: String = "active",
  address: Address = buildAddress(),
) = Accommodation(
  id = id,
  type = type,
  settledType = settledType,
  status = status,
  address = address,
)

fun buildAddress(
  postcode: String = "SW1A 1AA",
  subBuildingName: String? = null,
  buildingName: String? = null,
  buildingNumber: String? = "10",
  thoroughfareName: String? = "Downing Street",
  dependentLocality: String? = null,
  postTown: String? = "London",
  county: String? = null,
  country: String? = "England",
  uprn: String? = null,
) = Address(
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

fun buildDutyToRefer(
  id: UUID = UUID.randomUUID(),
  status: String = "submitted",
) = DutyToRefer(
  id = id,
  status = status,
)

fun buildCrs(
  id: UUID = UUID.randomUUID(),
  status: String = "submitted",
) = Crs(
  id = id,
  status = status,
)
