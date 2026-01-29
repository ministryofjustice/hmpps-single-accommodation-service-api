package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local", "dev")
class DevCasesConfig {
  @Bean(name = ["crnList"])
  fun crnList(): List<String> = listOf(
    "X371199",
    "X968879",
    "X966926",
    "X969031",
    "X980138",
    "X980067",
    "X980064",
    "X979948",
    "X979953",
    "X979496",
    "X979483",
    "X979299",
    "X979294",
    "X979218",
  )
}

@Configuration
@Profile("preprod", "prod")
class PreprodCasesConfig {
  @Bean(name = ["crnList"])
  fun crnList(): List<String> = listOf("E220130", "E341869", "E614509", "M601327")
}
