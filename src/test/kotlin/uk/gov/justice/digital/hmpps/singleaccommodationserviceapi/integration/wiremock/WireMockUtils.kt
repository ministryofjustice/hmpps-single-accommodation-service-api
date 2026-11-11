package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockManager.Companion.sasWiremock

object WireMockUtils {
  fun resolveWireMockUrl(path: String) = "${sasWiremock.baseUrl()}$path"
}
