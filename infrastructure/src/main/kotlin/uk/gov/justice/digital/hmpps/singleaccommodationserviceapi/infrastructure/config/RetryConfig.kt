package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.annotation.Retryable
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.util.isTimeout

@Configuration
@EnableRetry
class RetryConfig {
  private val log = LoggerFactory.getLogger(javaClass)

  object DefaultRetryDecider : RetryDecider {
    override fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
      // TODO: Once we have completed the switch to WebClient, we should remove the first two entries
      is HttpServerErrorException -> true
      is ResourceAccessException -> true
      is WebClientRequestException -> throwable.isTimeout()
      is WebClientResponseException -> throwable.statusCode.is5xxServerError
      else -> false
    }
  }

  @Bean
  fun retryListener(): RetryListener = object : RetryListener {

    override fun <T, E : Throwable?> onError(
      context: RetryContext,
      callback: RetryCallback<T, E>,
      throwable: Throwable,
    ) {
      if (!DefaultRetryDecider.shouldRetry(throwable)) return

      log.warn(
        "Retryable error occurred for {}. Retry attempt {} due to {}: {}",
        context.getAttribute(RetryContext.NAME) ?: "unknown call",
        context.retryCount,
        throwable.javaClass.simpleName,
        throwable.message,
      )
    }
  }

  @Bean
  fun retryDecider(): RetryDecider = DefaultRetryDecider
}

interface RetryDecider {
  fun shouldRetry(throwable: Throwable): Boolean
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Retryable(
  maxAttemptsExpression = $$"${spring.retry.rest-client.max-attempts}",
  backoff = Backoff(
    delayExpression = $$"${spring.retry.rest-client.initial-interval}",
    multiplierExpression = $$"${spring.retry.rest-client.multiplier}",
    maxDelayExpression = $$"${spring.retry.rest-client.max-interval}",
  ),
  exceptionExpression = "@retryDecider.shouldRetry(#root)",
)
annotation class RestClientRetry
