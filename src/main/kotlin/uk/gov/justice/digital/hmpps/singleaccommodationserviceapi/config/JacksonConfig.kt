package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val CANONICAL_INSTANT_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    .withZone(ZoneOffset.UTC)

@Configuration
class JacksonConfig {

  @Bean
  fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
    val module = SimpleModule().apply {
      addSerializer(Instant::class.java, InstantCanonicalSerializer())
      addDeserializer(Instant::class.java, InstantIsoDeserializer())
    }

    builder.modulesToInstall(module)
    builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }
}

class InstantCanonicalSerializer : JsonSerializer<Instant>() {
  override fun serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(
      CANONICAL_INSTANT_FORMATTER.format(
        value.truncatedTo(ChronoUnit.MICROS),
      ),
    )
  }
}

class InstantIsoDeserializer : JsonDeserializer<Instant>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant = Instant.parse(p.text)
}
