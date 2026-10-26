package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Test-only scheduling control. Production uses Spring Boot's default scheduler and always runs
 * scheduled jobs. In tests this replaces it (Boot's default backs off when a scheduler bean exists)
 * with one that can be paused, so message processing is off by default and switched on per test via
 * `@EnableMessageProcessing`, all on a single shared Spring context.
 */
@Configuration
@EnableConfigurationProperties(TestSchedulingProperties::class)
class TestSchedulingConfig {
  @Bean
  fun taskScheduler(
    properties: TestSchedulingProperties,
    @Value($$"${spring.threads.virtual.enabled:false}") virtualThreads: Boolean,
  ): PausableTaskScheduler = PausableTaskScheduler(properties).apply {
    setThreadNamePrefix("scheduling-")
    // Mirror Spring Boot's auto-configured scheduler, which uses virtual threads when this is enabled.
    setVirtualThreads(virtualThreads)
  }
}

/** Initial value comes from `scheduling.enabled` (false in the test profile); tests may flip it at runtime. */
@ConfigurationProperties(prefix = "scheduling")
class TestSchedulingProperties(
  enabled: Boolean,
) {
  @Volatile
  var enabled: Boolean = enabled
}

/**
 * Scheduler that only runs triggered tasks while [TestSchedulingProperties.enabled] is true.
 *
 * Each task holds a read lock across both the enabled check and its execution. [awaitIdle] takes the
 * write lock, so once the flag has been set to false it returns only after every task that passed the
 * check has finished, and any later task sees the flag as false.
 */
class PausableTaskScheduler(
  private val properties: TestSchedulingProperties,
) : SimpleAsyncTaskScheduler() {
  private val executionLock = ReentrantReadWriteLock()

  override fun schedule(task: Runnable, trigger: Trigger) = super.schedule(whenEnabled(task), trigger)

  override fun schedule(task: Runnable, startTime: Instant) = super.schedule(whenEnabled(task), startTime)

  override fun scheduleAtFixedRate(task: Runnable, startTime: Instant, period: Duration) =
    super.scheduleAtFixedRate(whenEnabled(task), startTime, period)

  override fun scheduleAtFixedRate(task: Runnable, period: Duration) = super.scheduleAtFixedRate(whenEnabled(task), period)

  override fun scheduleWithFixedDelay(task: Runnable, startTime: Instant, delay: Duration) =
    super.scheduleWithFixedDelay(whenEnabled(task), startTime, delay)

  override fun scheduleWithFixedDelay(task: Runnable, delay: Duration) = super.scheduleWithFixedDelay(whenEnabled(task), delay)

  private fun whenEnabled(task: Runnable) = Runnable {
    executionLock.readLock().withLock {
      if (properties.enabled) {
        task.run()
      }
    }
  }

  /** Waits for in-flight tasks to finish. Call after setting [TestSchedulingProperties.enabled] to false. */
  fun awaitIdle(timeout: Duration = DEFAULT_AWAIT_IDLE_TIMEOUT) {
    check(executionLock.writeLock().tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
      "Scheduled tasks still running after $timeout"
    }
    executionLock.writeLock().unlock()
  }

  private companion object {
    val DEFAULT_AWAIT_IDLE_TIMEOUT: Duration = Duration.ofSeconds(10)
  }
}

