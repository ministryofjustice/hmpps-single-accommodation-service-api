package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HttpAuthService {
  fun getPrincipalOrThrow(acceptableSources: List<String>): Principal {
    val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    if (!acceptableSources.contains(authToken.principal.authSource.source)) {
      throw AccessDeniedException("JWT token does not contain auth_source claim")
    }
    return authToken.principal
  }

  fun setPrincipalUserId(sasUserId: UUID) {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    principal.principal.sasUserId = sasUserId
  }

  fun getJwt(): Jwt {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    return principal.token
  }
}
