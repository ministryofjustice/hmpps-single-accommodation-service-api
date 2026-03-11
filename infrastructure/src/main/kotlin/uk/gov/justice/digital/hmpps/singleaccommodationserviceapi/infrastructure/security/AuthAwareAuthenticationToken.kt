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
  val username: Username,
  override val authSource: AuthSource,
) : Principal

data class ClientCredentialsPrincipal(
  val clientId: String,
  override val authSource: AuthSource,
) : Principal

@JvmInline
value class Username private constructor(val value: String) {

  init {
    require(value.isNotBlank()) { "Username must not be blank" }
  }

  companion object {
    operator fun invoke(value: String): Username = Username(value.uppercase())
  }

  override fun toString(): String = value
}
