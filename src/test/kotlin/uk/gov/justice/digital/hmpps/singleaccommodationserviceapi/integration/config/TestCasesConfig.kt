package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestCasesConfig {
  @Bean(name = ["crnList"])
  fun crnList(): List<String> = listOf("X371199", "X968879", "X966926", "X969031")
}
