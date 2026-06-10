package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.interceptor.RetryInterceptorBuilder
import org.springframework.retry.interceptor.RetryOperationsInterceptor
import java.time.Duration

@Configuration
@EnableRetry
class RetryConfig(
  @Value($$"${spring.retry.max-attempts:3}")
  val maxAttemps: Int,
  @Value($$"${spring.retry.initial-interval:50ms}")
  val delay: Duration,
  @Value($$"${spring.retry.multiplier:1.0}")
  val multiplier: Double,
  @Value($$"${spring.retry.max-interval:2s}")
  val maxInterval: Duration,

) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Bean
  fun retryInterceptor(): RetryOperationsInterceptor = RetryInterceptorBuilder.stateless()
    .maxAttempts(maxAttemps)
    .backOffOptions(delay.toMillis(), multiplier, maxInterval.toMillis())
    .build()

  @Bean
  fun retryListener(): RetryListener = object : RetryListener {

    override fun <T, E : Throwable?> onError(
      context: RetryContext,
      callback: RetryCallback<T, E>,
      throwable: Throwable,
    ) {
      log.warn(
        "*** Retry attempt {} due to {}: {}",
        context.retryCount,
        throwable.javaClass.simpleName,
        throwable.message,
      )
    }
  }
}
