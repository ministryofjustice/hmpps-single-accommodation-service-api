package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import java.util.*

@TestConfiguration
class TestJpaAuditorConfig(
  @Value($$"${test-data-setup.user-id}")
  private val testDataSetupUserId: UUID,
) {

  @Primary
  @Bean
  fun auditorAware(): AuditorAware<UUID> = AuditorAware {
    try {
      val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
      Optional.of(authToken.principal.userUuid)
    } catch (_: Exception) {
      Optional.of(testDataSetupUserId)
    }
  }
}
