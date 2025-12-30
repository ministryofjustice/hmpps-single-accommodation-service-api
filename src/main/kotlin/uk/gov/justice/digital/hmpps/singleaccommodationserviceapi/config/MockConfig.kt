package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock.getMockedData

@Configuration
@Profile("local", "dev")
class MockConfig {
  @Bean
  fun mockedData(): MockData = getMockedData()
}
