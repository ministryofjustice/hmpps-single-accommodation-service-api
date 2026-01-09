package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local", "dev")
class DevCasesConfig {
  @Bean(name = ["crnList"])
  fun crnList(): List<String> = listOf("X371199", "X968879", "X966926", "X969031")
}

@Configuration
@Profile("preprod", "prod")
class PreprodCasesConfig {
  @Bean(name = ["crnList"])
  fun crnList(): List<String> = listOf("E220130", "E341869", "E614509", "M601327")
}
