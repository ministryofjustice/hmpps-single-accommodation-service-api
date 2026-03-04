package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

@Profile(value = ["local", "dev", "preprod", "prod"])
@Configuration
class ClockConfig {
  @Bean
  fun clock(): Clock = Clock.systemUTC()
}
