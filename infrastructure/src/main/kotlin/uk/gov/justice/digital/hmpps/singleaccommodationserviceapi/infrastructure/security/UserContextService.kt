package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Service
class UserContextService(
  private val userService: UserService,
) {
  private val sasSystemUser by lazy { userService.getSystemUser() }

  fun setUserContextAsSasSystemUser() {
    val principal = UserPrincipal(
      sasUserId = sasSystemUser.id,
      username = Username(sasSystemUser.username),
      authSource = AuthSource.NONE,
    )
    val systemAuthentication = UsernamePasswordAuthenticationToken(
      principal,
      "N/A",
      emptyList(),
    )
    val context = SecurityContextHolder.createEmptyContext()
    context.authentication = systemAuthentication
    SecurityContextHolder.setContext(context)
  }

  fun clearContext() {
    SecurityContextHolder.clearContext()
  }
}
