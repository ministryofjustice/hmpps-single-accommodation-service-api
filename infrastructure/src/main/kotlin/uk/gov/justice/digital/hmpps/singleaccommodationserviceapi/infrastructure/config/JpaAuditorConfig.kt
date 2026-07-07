package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.Optional
import java.util.UUID

@Configuration
@EnableJpaAuditing
class JpaAuditorConfig {

  @Bean
  fun auditorAware(): AuditorAware<UUID> = AuditorAware {
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
  }
}
