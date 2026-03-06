package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.osplaces

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface OsPlacesClient {
  @GetExchange("/postcode")
  fun searchByPostcode(
    @RequestParam("postcode") postcode: String,
    @RequestParam("dataset") dataset: String = "LPI",
    @RequestParam("maxResults") maxResults: Int = 50,
    @RequestParam("offset") offset: Int = 0,
  ): OsPlacesResponse
}

data class OsPlacesResponse(
  val header: OsPlacesHeader,
  val results: List<OsPlacesResultWrapper> = emptyList(),
)

data class OsPlacesHeader(
  val totalresults: Int,
  val maxresults: Int,
  val offset: Int,
)

data class OsPlacesResultWrapper(
  val LPI: OsPlacesLpi? = null,
)

data class OsPlacesLpi(
  val UPRN: String?,
  val ADDRESS: String?,
  val PAO_TEXT: String?,
  val STREET_DESCRIPTION: String?,
  val LOCALITY_NAME: String?,
  val TOWN_NAME: String?,
  val ADMINISTRATIVE_AREA: String?,
  val POSTCODE_LOCATOR: String?,
  val CLASSIFICATION_CODE: String?,
  val CLASSIFICATION_CODE_DESCRIPTION: String?,
  val STATUS: String?,
  val X_COORDINATE: Double?,
  val Y_COORDINATE: Double?,
  val MATCH: Double?,
  val MATCH_DESCRIPTION: String?,
  val SAO_TEXT: String?,
  val PAO_START_NUMBER: String?,
  val COUNTRY_CODE_DESCRIPTION: String?,
)
