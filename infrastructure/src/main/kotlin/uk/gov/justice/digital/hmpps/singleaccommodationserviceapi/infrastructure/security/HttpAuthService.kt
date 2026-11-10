package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Service
class HttpAuthService {

  private val authSources = listOf(AuthSource.DELIUS.source, AuthSource.NOMIS.source)

  fun getPrincipalOrThrow(): UserPrincipal {
    val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    if (!authSources.contains(authToken.principal.authSource.source)) {
      throw AccessDeniedException("JWT token does not contain auth_source claim")
    }
    return authToken.principal as UserPrincipal
  }

  fun getUsername() = getPrincipalOrThrow().username

  fun getJwt(): Jwt {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    return principal.token
  }
}
