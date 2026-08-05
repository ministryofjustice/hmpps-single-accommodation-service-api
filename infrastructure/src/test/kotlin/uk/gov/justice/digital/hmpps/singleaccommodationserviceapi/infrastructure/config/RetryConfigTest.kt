package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.annotation.Retryable
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.nio.charset.StandardCharsets

class RetryConfigTest {

  private val retryListener = RetryConfig().retryListener()
  private val retryContext = mockk<RetryContext> { every { retryCount } returns 1 }
  private val callback = mockk<RetryCallback<Any, Throwable?>>()

  @ParameterizedTest(name = "logs retry warning for {0}")
  @FieldSource("retryableExceptions")
  fun `logs retry warning for configured retryable exceptions`(throwable: Throwable) {
    withLogCapture { appender ->
      retryListener.onError(retryContext, callback, throwable)

      assertThat(appender.list).hasSize(1)
      assertThat(appender.list.single().level).isEqualTo(Level.WARN)
      assertThat(appender.list.single().formattedMessage)
        .contains("Retryable error occurred. Retry attempt 1")
        .contains(throwable.javaClass.simpleName)
    }
  }

  @ParameterizedTest(name = "does not log retry warning for {0}")
  @FieldSource("nonRetryableExceptions")
  fun `does not log retry warning for non retryable exceptions`(throwable: Throwable) {
    withLogCapture { appender ->
      retryListener.onError(retryContext, callback, throwable)

      assertThat(appender.list).isEmpty()
    }
  }

  @Test
  fun `RestClientRetry annotation includes all expected retryable exception types`() {
    val retryable = RestClientRetry::class.java.getAnnotation(Retryable::class.java)
    val configuredTypes = retryable.value.map { it.java }.toSet()

    assertThat(configuredTypes).containsExactlyInAnyOrder(
      HttpServerErrorException::class.java,
      ResourceAccessException::class.java,
    )
  }

  private fun withLogCapture(block: (ListAppender<ILoggingEvent>) -> Unit) {
    val logger = LoggerFactory.getLogger(RetryConfig::class.java) as Logger
    val appender = ListAppender<ILoggingEvent>().apply { start() }
    logger.addAppender(appender)
    try {
      block(appender)
    } finally {
      logger.detachAppender(appender)
    }
  }

  companion object {
    @JvmField
    val retryableExceptions = listOf(
      ResourceAccessException("socket timeout"),
      HttpServerErrorException.create(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        HttpHeaders.EMPTY,
        ByteArray(0),
        StandardCharsets.UTF_8,
      ),
    )

    @JvmField
    val nonRetryableExceptions = listOf(
      HttpClientErrorException.create(
        HttpStatus.NOT_FOUND,
        "Not Found",
        HttpHeaders.EMPTY,
        ByteArray(0),
        StandardCharsets.UTF_8,
      ),
      HttpClientErrorException.create(
        HttpStatus.FORBIDDEN,
        "Forbidden",
        HttpHeaders.EMPTY,
        ByteArray(0),
        StandardCharsets.UTF_8,
      ),
    )
  }
}
