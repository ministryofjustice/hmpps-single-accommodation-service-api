package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry

import io.sentry.Sentry
import io.sentry.SentryLevel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface SentryService {
  fun captureException(throwable: Throwable)
  fun captureErrorMessage(message: String)
}

@Service
class SentryServiceImpl : SentryService {
  override fun captureException(throwable: Throwable) {
    log.debug("Capturing exception in Sentry", throwable)
    Sentry.captureException(throwable)
  }

  override fun captureErrorMessage(message: String) {
    log.debug("Capturing error message in Sentry: {}", message)
    Sentry.captureMessage(message, SentryLevel.ERROR)
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
