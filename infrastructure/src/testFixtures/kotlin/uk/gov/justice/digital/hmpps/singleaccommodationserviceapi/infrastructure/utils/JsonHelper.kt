package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils

import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import java.time.OffsetDateTime
import java.time.ZoneOffset

object JsonHelper {

  @JvmStatic
  val jsonMapper: JsonMapper = JsonMapper.builder()
    .findAndAddModules()
    .build()
}

/**
 * Use instead of toString() on OffsetDateTime values which can produce fractional seconds differently and cause flakey tests
 */
fun OffsetDateTime.asJsonValue(): String = jsonMapper.writeValueAsString(withOffsetSameInstant(ZoneOffset.UTC)).removeSurrounding("\"")
