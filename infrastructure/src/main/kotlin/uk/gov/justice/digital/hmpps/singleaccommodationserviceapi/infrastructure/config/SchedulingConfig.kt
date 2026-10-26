package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.Duration
import java.time.Instant
import javax.sql.DataSource

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = $$"${shedlock.default-lock-at-most-for}")
class SchedulingConfig {
  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider = JdbcTemplateLockProvider(dataSource)

  @Bean
  fun taskScheduler(schedulingProperties: SchedulingProperties): ThreadPoolTaskScheduler = EnabledAwareTaskScheduler(schedulingProperties).apply {
    setThreadNamePrefix("scheduling-")
  }
}

/**
 * Scheduler that only runs triggered tasks while [SchedulingProperties.enabled] is true. The flag is
 * checked on every trigger, so it can be changed at runtime. Only scheduler-triggered executions are
 * affected; calling a `@Scheduled` method directly always runs it.
 */
class EnabledAwareTaskScheduler(
  private val schedulingProperties: SchedulingProperties,
) : ThreadPoolTaskScheduler() {
  override fun schedule(task: Runnable, trigger: Trigger) = super.schedule(whenEnabled(task), trigger)

  override fun schedule(task: Runnable, startTime: Instant) = super.schedule(whenEnabled(task), startTime)

  override fun scheduleAtFixedRate(task: Runnable, startTime: Instant, period: Duration) = super.scheduleAtFixedRate(whenEnabled(task), startTime, period)

  override fun scheduleAtFixedRate(task: Runnable, period: Duration) = super.scheduleAtFixedRate(whenEnabled(task), period)

  override fun scheduleWithFixedDelay(task: Runnable, startTime: Instant, delay: Duration) = super.scheduleWithFixedDelay(whenEnabled(task), startTime, delay)

  override fun scheduleWithFixedDelay(task: Runnable, delay: Duration) = super.scheduleWithFixedDelay(whenEnabled(task), delay)

  private fun whenEnabled(task: Runnable) = Runnable {
    if (schedulingProperties.enabled) {
      task.run()
    }
  }
}
