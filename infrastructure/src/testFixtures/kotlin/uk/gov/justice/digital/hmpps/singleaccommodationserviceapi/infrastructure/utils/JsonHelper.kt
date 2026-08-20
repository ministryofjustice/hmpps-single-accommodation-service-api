package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils

import tools.jackson.databind.json.JsonMapper

object JsonHelper {

  @JvmStatic
  val jsonMapper: JsonMapper = JsonMapper.builder()
    .findAndAddModules()
    .build()
}
