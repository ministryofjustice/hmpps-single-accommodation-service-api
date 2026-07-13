package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.AuditorAware
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.Optional
import java.util.UUID

@TestConfiguration
class TestJpaAuditorConfig(
  @Value($$"${test-data-setup.user-id}")
  private val testDataSetupUserId: UUID,
) {

  @Primary
  @Bean
  fun auditorAware(): AuditorAware<UUID> = AuditorAware {
    try {
      val auditOverrideId = AuditOverrideContext.currentAuditorId()
      if (auditOverrideId != null) {
        Optional.of(auditOverrideId)
      } else {
        val authentication = requireNotNull(SecurityContextHolder.getContext().authentication) {
          "No authentication in SecurityContext for auditing"
        }
        val userPrincipal = when (authentication) {
          is AuthAwareAuthenticationToken -> authentication.principal as UserPrincipal
          is UsernamePasswordAuthenticationToken -> authentication.principal as UserPrincipal
          else -> throw IllegalStateException("Unsupported authentication type for auditing: ${authentication::class.qualifiedName}")
        }
        Optional.of(userPrincipal.sasUserId)
      }
    } catch (_: Exception) {
      Optional.of(testDataSetupUserId)
    }
  }
}
