package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test configuration that wraps [TierClient] to record max concurrent handler invocations. Used to
 * prove the dispatcher's semaphore limits concurrency.
 */
@TestConfiguration
class SemaphoreConcurrencyTestConfig {

  @Bean fun concurrencyCounter() = ConcurrencyCounter()

  @Bean
  @Primary
  fun countingTierClient(
    @Qualifier("tierClient") delegate: TierClient,
    counter: ConcurrencyCounter,
  ): TierClient = object : TierClient {
    override fun getTier(uri: URI): Tier {
      counter.enter()
      try {
        Thread.sleep(150)
        return delegate.getTier(uri)
      } finally {
        counter.exit()
      }
    }

    override fun getTier(crn: String) = delegate.getTier(crn)
  }
}

class ConcurrencyCounter(
  val currentCount: AtomicInteger = AtomicInteger(0),
  val maxConcurrent: AtomicInteger = AtomicInteger(0),
) {
  fun enter() {
    val c = currentCount.incrementAndGet()
    maxConcurrent.updateAndGet { maxOf(it, c) }
  }

  fun exit() {
    currentCount.decrementAndGet()
  }
}
