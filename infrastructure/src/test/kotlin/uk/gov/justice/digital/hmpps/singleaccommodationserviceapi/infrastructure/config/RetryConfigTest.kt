package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.nio.charset.StandardCharsets

class RetryConfigTest {

  @Test
  fun `logs retry warning for configured retryable exceptions`() {
    val logger = LoggerFactory.getLogger(RetryConfig::class.java) as Logger
    val listAppender = ListAppender<ILoggingEvent>().apply { start() }
    logger.addAppender(listAppender)

    try {
      val retryListener = RetryConfig().retryListener()
      val retryContext = mockk<RetryContext> {
        every { retryCount } returns 1
      }
      val callback = mockk<RetryCallback<Any, Throwable?>>()

      retryListener.onError(retryContext, callback, ResourceAccessException("socket timeout"))

      assertThat(listAppender.list).hasSize(1)
      assertThat(listAppender.list.single().level).isEqualTo(Level.WARN)
      assertThat(listAppender.list.single().formattedMessage)
        .contains("Retryable error occurred. Retry attempt 1")
        .contains("ResourceAccessException")
    } finally {
      logger.detachAppender(listAppender)
    }
  }

  @Test
  fun `does not log retry warning for non retryable exceptions`() {
    val logger = LoggerFactory.getLogger(RetryConfig::class.java) as Logger
    val listAppender = ListAppender<ILoggingEvent>().apply { start() }
    logger.addAppender(listAppender)

    try {
      val retryListener = RetryConfig().retryListener()
      val retryContext = mockk<RetryContext> {
        every { retryCount } returns 1
      }
      val callback = mockk<RetryCallback<Any, Throwable?>>()
      val notFound = HttpClientErrorException.create(
        HttpStatus.NOT_FOUND,
        "Not Found",
        HttpHeaders.EMPTY,
        ByteArray(0),
        StandardCharsets.UTF_8,
      )

      retryListener.onError(retryContext, callback, notFound)

      assertThat(listAppender.list).isEmpty()
    } finally {
      logger.detachAppender(listAppender)
    }
  }
}
