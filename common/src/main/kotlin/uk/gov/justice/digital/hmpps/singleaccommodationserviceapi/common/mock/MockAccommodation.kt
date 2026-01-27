package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import java.time.Instant
import java.util.UUID
import kotlin.String

fun getMockedAccommodation(availableCrnList: List<String>, crn: String) = AccommodationDto(
  crn = crn,
  current = getMockedCurrentAccommodation(availableCrnList, crn),
  next = getMockedNextAccommodation(availableCrnList, crn),
)

fun getMockedNextAccommodation(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> AccommodationDetail(
    id = UUID.fromString("b697a854-96af-4360-a715-189a78d4f70f"),
    name = null,
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = AccommodationStatus.CONFIRMED,
    address = AccommodationAddressDetails(
       postcode = "RG26 5AG",
       subBuildingName = null,
       buildingName = null,
       buildingNumber = "4",
       thoroughfareName = "Dollis Green",
       dependentLocality = null,
       postTown = "Bramley",
       county = null,
       country = null,
       uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2026-01-20T16:07:20.120069Z")
  )

  availableCrnList[1] -> AccommodationDetail(
    id = UUID.fromString("fa75a728-1020-44d0-8bb6-343ca1197d2e"),
    name = null,
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.SOCIAL_RENTED,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.SETTLED,
    offenderReleaseType = null,
    status = AccommodationStatus.NOT_CHECKED_YET,
    address = AccommodationAddressDetails(
      postcode = "W1 8XX",
      subBuildingName = null,
      buildingName = null,
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      dependentLocality = null,
      postTown = "London",
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2026-01-15T12:19:20.120069Z")
  )

  availableCrnList[2] -> AccommodationDetail(
    id = UUID.fromString("a32ab37c-8830-4806-b6d6-72da561ce1ee"),
    name = null,
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.PRIVATE_RENTED_ROOM,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.SETTLED,
    offenderReleaseType = null,
    status = AccommodationStatus.NOT_CHECKED_YET,
    address = AccommodationAddressDetails(
      postcode = "SL2 2BP",
      subBuildingName = null,
      buildingName = null,
      buildingNumber = "52",
      thoroughfareName = "Odencroft Road",
      dependentLocality = null,
      postTown = "Slough",
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2026-01-08T07:22:20.120069Z")
  )

  availableCrnList[3] -> AccommodationDetail(
    id = UUID.fromString("c46099b9-65b0-4742-89db-871e60bbc982"),
    name = null,
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.OTHER,
    arrangementSubTypeDescription = "Caravan",
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = AccommodationStatus.NOT_CHECKED_YET,
    address = AccommodationAddressDetails(
      postcode = "AB12 3CD",
      subBuildingName = null,
      buildingName = null,
      buildingNumber = "15",
      thoroughfareName = "Stonecross Road",
      dependentLocality = null,
      postTown = "Brighton",
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2026-01-02T05:12:10.120069Z")
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCurrentAccommodation(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> AccommodationDetail(
    id = UUID.fromString("f3813060-59c7-48ff-8729-3ea6efbf375b"),
    name = "HMP Huntercombe",
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription  = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2023-01-02T11:07:09.120069Z")
  )
  availableCrnList[1] -> AccommodationDetail(
    id = UUID.fromString("f296b6a7-79c3-4d46-b5ed-683e72e9ae09"),
    name = "HMP Bullingdon",
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2021-11-11T14:35:11.120069Z")
  )
  availableCrnList[2] -> AccommodationDetail(
    id = UUID.fromString("b697a854-96af-4360-a715-189a78d4f70f"),
    name = "HMP Huntercombe",
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("2020-05-08T16:07:15.120069Z")
  )
  availableCrnList[3] -> AccommodationDetail(
    id = UUID.fromString("5b845756-7760-45bb-b756-c6b112e9778c"),
    name = "HMP Bullingdon",
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null,
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.parse("1995-01-18T09:45:10.120069Z")
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
