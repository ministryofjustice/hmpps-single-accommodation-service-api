package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.getMockedData

@TestConfiguration
class TestMockConfig {

  @Bean
  fun mockedData(crnList: List<String>): MockData = getMockedData(availableCrnList = crnList)
}
