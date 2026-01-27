package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils

import tools.jackson.databind.json.JsonMapper

object JsonHelper {

  @JvmStatic
  val jsonMapper: JsonMapper = JsonMapper.builder()
    .build()
}
