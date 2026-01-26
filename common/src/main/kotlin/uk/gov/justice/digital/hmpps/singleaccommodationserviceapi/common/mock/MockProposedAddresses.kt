package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import java.util.UUID

fun getMockedProposedAddresses(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> listOf(
    AccommodationDetail(
      id = UUID.fromString("6d9a38c4-a8f6-49d1-856d-972906f63361"),
      arrangementType = AccommodationArrangementType.PRIVATE,
      arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
      arrangementSubTypeDescription = null,
      settledType = AccommodationSettledType.SETTLED,
      status = AccommodationStatus.NOT_CHECKED_YET,
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
    ),
    AccommodationDetail(
      id = UUID.fromString("f03aac3e-2f36-4003-a753-db571fe140b8"),
      arrangementType = AccommodationArrangementType.PRIVATE,
      arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
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
  )
  availableCrnList[1] -> listOf(
    AccommodationDetail(
      id = UUID.fromString("3010ee1c-30f1-4621-81b2-87c349104898"),
      arrangementType = AccommodationArrangementType.PRIVATE,
      arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
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
  )
  availableCrnList[2] -> listOf(
    AccommodationDetail(
      id = UUID.fromString("b0410f55-16d9-4509-b837-273504d95f8f"),
      arrangementType = AccommodationArrangementType.PRIVATE,
      arrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
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
  )
  availableCrnList[3] -> listOf()
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
