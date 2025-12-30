package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress.AddedBy
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress.AddressDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress.PrivateAddressesDto
import java.time.LocalDateTime

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
          addedDate = LocalDateTime.parse("1970-01-01T00:00:00"),
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
          addedDate = LocalDateTime.parse("1970-01-01T00:00:00"),
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
          addedDate = LocalDateTime.parse("1970-01-01T00:00:00"),
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
          addedDate = LocalDateTime.parse("1970-01-01T00:00:00"),
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
