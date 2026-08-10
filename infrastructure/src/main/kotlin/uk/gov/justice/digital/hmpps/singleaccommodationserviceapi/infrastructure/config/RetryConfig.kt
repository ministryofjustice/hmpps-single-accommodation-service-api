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

@Configuration
@EnableRetry
class RetryConfig {
  private val log = LoggerFactory.getLogger(javaClass)

  // read the retryable exception types from the annotation, for single source of truth
  private val retryableExceptions: Set<Class<out Throwable>> =
    RestClientRetry::class.java.getAnnotation(Retryable::class.java)?.value?.map { it.java }?.toSet().orEmpty()

  @Bean
  fun retryListener(): RetryListener = object : RetryListener {

    override fun <T, E : Throwable?> onError(
      context: RetryContext,
      callback: RetryCallback<T, E>,
      throwable: Throwable,
    ) {
      if (retryableExceptions.none { it.isAssignableFrom(throwable.javaClass) }) return

      log.warn(
        "Retryable error occurred. Retry attempt {} due to {}: {}",
        context.retryCount,
        throwable.javaClass.simpleName,
        throwable.message,
      )
    }
  }
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
  value = [
    HttpServerErrorException::class,
    ResourceAccessException::class,
  ],
)
annotation class RestClientRetry
