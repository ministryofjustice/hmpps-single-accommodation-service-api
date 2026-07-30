package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.javers.spring.auditable.AuthorProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.UUID

@TestConfiguration
class TestJaversAuthProvider(
  @Value($$"${test-data-setup.user-id}")
  private val testDataSetupUserId: UUID,
) : AuthorProvider {
  override fun provide(): String {
    try {
      val auditOverrideId = AuditOverrideContext.currentAuditorId()
      return if (auditOverrideId != null) {
        auditOverrideId.toString()
      } else {
        val authentication = requireNotNull(SecurityContextHolder.getContext().authentication) {
          "No authentication in SecurityContext for auditing"
        }
        val userPrincipal = when (authentication) {
          is AuthAwareAuthenticationToken -> authentication.principal as UserPrincipal
          is UsernamePasswordAuthenticationToken -> authentication.principal as UserPrincipal
          else -> throw IllegalStateException("Unsupported authentication type for auditing: ${authentication::class.qualifiedName}")
        }
        userPrincipal.sasUserId.toString()
      }
    } catch (_: Exception) {
      return testDataSetupUserId.toString()
    }
  }
}
