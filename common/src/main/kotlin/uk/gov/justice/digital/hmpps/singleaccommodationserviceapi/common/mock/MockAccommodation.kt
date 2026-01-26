package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
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
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    status = AccommodationStatus.PASSED,
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
  )

  availableCrnList[1] -> AccommodationDetail(
    id = UUID.fromString("fa75a728-1020-44d0-8bb6-343ca1197d2e"),
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.SOCIAL_RENTED,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.SETTLED,
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
  )

  availableCrnList[2] -> AccommodationDetail(
    id = UUID.fromString("a32ab37c-8830-4806-b6d6-72da561ce1ee"),
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.PRIVATE_RENTED_ROOM,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.SETTLED,
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
  )

  availableCrnList[3] -> AccommodationDetail(
    id = UUID.fromString("c46099b9-65b0-4742-89db-871e60bbc982"),
    arrangementType = AccommodationArrangementType.PRIVATE,
    arrangementSubType = AccommodationArrangementSubType.OTHER,
    arrangementSubTypeDescription = "Caravan",
    settledType = AccommodationSettledType.TRANSIENT,
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
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCurrentAccommodation(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> AccommodationDetail(
    id = UUID.fromString("f3813060-59c7-48ff-8729-3ea6efbf375b"),
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription  = null,
    settledType = AccommodationSettledType.TRANSIENT,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = "HMP Huntercombe",
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
  )
  availableCrnList[1] -> AccommodationDetail(
    id = UUID.fromString("f296b6a7-79c3-4d46-b5ed-683e72e9ae09"),
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = "HMP Bullingdon",
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
  )
  availableCrnList[2] -> AccommodationDetail(
    id = UUID.fromString("b697a854-96af-4360-a715-189a78d4f70f"),
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = "HMP Huntercombe",
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
  )
  availableCrnList[3] -> AccommodationDetail(
    id = UUID.fromString("5b845756-7760-45bb-b756-c6b112e9778c"),
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    status = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = "HMP Bullingdon",
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
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
