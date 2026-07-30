package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@ConditionalOnProperty(value = ["scheduling.enabled"], havingValue = "true")
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = $$"${shedlock.default-lock-at-most-for}")
class SchedulingConfig {
  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider = JdbcTemplateLockProvider(dataSource)
}
