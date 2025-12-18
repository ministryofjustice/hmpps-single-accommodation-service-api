package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.getMockedData

@TestConfiguration
class TestMockConfig {

  @Bean
  fun mockedData(): MockData = getMockedData()
}
