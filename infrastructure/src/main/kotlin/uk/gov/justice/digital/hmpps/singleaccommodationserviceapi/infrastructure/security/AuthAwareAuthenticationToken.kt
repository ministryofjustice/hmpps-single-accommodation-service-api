package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val principal: Principal,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): Principal = principal
}

interface Principal {
  val authSource: AuthSource
}

data class UserPrincipal(
  var sasUserId: UUID,
  val username: String,
  override val authSource: AuthSource,
) : Principal

data class ClientCredentialsPrincipal(
  val clientId: String,
  override val authSource: AuthSource,
) : Principal
