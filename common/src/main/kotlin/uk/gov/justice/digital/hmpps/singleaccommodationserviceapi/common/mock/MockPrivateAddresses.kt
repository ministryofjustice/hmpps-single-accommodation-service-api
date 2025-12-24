package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddedBy
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddressDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PrivateAddressesDto

fun getMockedPrivateAddresses(crn: String) = when (crn) {
  mockCrns[0] ->
    PrivateAddressesDto(
      crn = crn,
      addresses = listOf(
        AddressDto(
          id = "p5e22e29-36bf-48e5-bfc9-915176298cb0",
          status = "checked",
          address = AddressDetails(
            line1 = "Flat 7",
            line2 = "20 Main Road",
            region = "Oxford",
            city = "Oxford",
            postcode = "OX2 6ZZ",
          ),
          addedBy = AddedBy(
            id = "user-1237",
            name = "Danny Smith",
            role = "Probation Officer",
          ),
          addedDate = mockedLocalDateTime,
        ),
        AddressDto(
          id = "l5e22e29-36bf-48e5-bfc9-915176298cb0",
          status = "unsuitable",
          address = AddressDetails(
            line1 = "Flat 9",
            line2 = "70 Main Road",
            region = "Oxford",
            city = "Oxford",
            postcode = "OX2 6ZZ",
          ),
          addedBy = AddedBy(
            id = "user-1234",
            name = "Peter Smith",
            role = "Probation Officer",
          ),
          addedDate = mockedLocalDateTime,
        ),
      ),
    )
  mockCrns[1] ->
    PrivateAddressesDto(
      crn = crn,
      addresses = listOf(
        AddressDto(
          id = "n5e22e29-36bf-48e5-bfc9-915176298cb0",
          status = "not_checked",
          address = AddressDetails(
            line1 = "Flat 4",
            line2 = "12 Main Road",
            region = "Oxford",
            city = "Oxford",
            postcode = "OX2 6ZZ",
          ),
          addedBy = AddedBy(
            id = "user-123a",
            name = "Mike Smith",
            role = "Probation Officer",
          ),
          addedDate = mockedLocalDateTime,
        ),
      ),

    )
  mockCrns[2] ->
    PrivateAddressesDto(
      crn = crn,
      addresses = listOf(
        AddressDto(
          id = "a5e22e29-36bf-48e5-bfc9-915176298cb0",
          status = "not_checked",
          address = AddressDetails(
            line1 = "Flat 2",
            line2 = "14 Fallowbank Road",
            region = "Oxford",
            city = "Oxford",
            postcode = "OX2 6ZZ",
          ),
          addedBy = AddedBy(
            id = "user-123",
            name = "Angel Smith",
            role = "Prison Admin",
          ),
          addedDate = mockedLocalDateTime,
        ),
      ),
    )
  mockCrns[3] ->
    PrivateAddressesDto(
      crn = crn,
      addresses = listOf(),
    )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
