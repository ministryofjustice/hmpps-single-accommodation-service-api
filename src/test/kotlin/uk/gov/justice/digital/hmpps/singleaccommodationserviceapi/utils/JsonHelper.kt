package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.InstantCanonicalSerializer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.InstantIsoDeserializer
import java.time.Instant

object JsonHelper {

  val instantModule = SimpleModule().apply {
    addSerializer(Instant::class.java, InstantCanonicalSerializer())
    addDeserializer(Instant::class.java, InstantIsoDeserializer())
  }

  @JvmStatic
  val objectMapper: ObjectMapper =
    jacksonObjectMapper()
      .registerModule(instantModule)
      .registerModule(JavaTimeModule())
      .registerKotlinModule()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
}
