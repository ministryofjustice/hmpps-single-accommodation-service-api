package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.addresslookup

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Address
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddressLookupResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.osplaces.OsPlacesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.osplaces.OsPlacesLpi

@Service
class AddressLookupService(
  private val osPlacesClient: OsPlacesClient,
) {
  fun byPostcode(postcode: String, maxResults: Int, offset: Int): AddressLookupResponse {
    val response = osPlacesClient.searchByPostcode(postcode = postcode, maxResults = maxResults, offset = offset)
    val addresses = response.results.mapNotNull { it.LPI }.map { it.toAddress() }
    return AddressLookupResponse(
      addresses = addresses,
      total = response.header.totalresults,
      offset = response.header.offset,
      maxResults = response.header.maxresults,
    )
  }
}

private fun OsPlacesLpi.toAddress() = Address(
  uprn = UPRN,
  singleLine = ADDRESS,
  buildingName = PAO_TEXT,
  buildingNumber = PAO_START_NUMBER,
  subBuildingName = SAO_TEXT,
  street = STREET_DESCRIPTION,
  locality = LOCALITY_NAME,
  town = TOWN_NAME,
  county = ADMINISTRATIVE_AREA,
  country = COUNTRY_CODE_DESCRIPTION,
  postcode = POSTCODE_LOCATOR,
  classification = CLASSIFICATION_CODE_DESCRIPTION ?: CLASSIFICATION_CODE,
  status = STATUS,
  x = X_COORDINATE,
  y = Y_COORDINATE,
  match = MATCH,
  matchDescription = MATCH_DESCRIPTION,
)
