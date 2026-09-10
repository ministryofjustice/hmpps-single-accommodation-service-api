package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import java.util.concurrent.CopyOnWriteArrayList

@TestConfiguration
class SentryCaptureTestConfig {
  @Bean
  @Primary
  fun testSentryService() = TestSentryService()
}

class TestSentryService : SentryService {
  val exceptions = CopyOnWriteArrayList<Throwable>()
  val errorMessages = CopyOnWriteArrayList<String>()

  override fun captureException(throwable: Throwable) {
    exceptions.add(throwable)
  }

  override fun captureErrorMessage(message: String) {
    errorMessages.add(message)
  }

  fun reset() {
    exceptions.clear()
    errorMessages.clear()
  }
}
