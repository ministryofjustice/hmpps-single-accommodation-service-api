package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.getMockedData

@Configuration
@Profile("local", "dev")
class MockConfig {
  @Bean
  fun mockedData(crnList: List<String>): MockData = getMockedData(availableCrnList = crnList)
}
