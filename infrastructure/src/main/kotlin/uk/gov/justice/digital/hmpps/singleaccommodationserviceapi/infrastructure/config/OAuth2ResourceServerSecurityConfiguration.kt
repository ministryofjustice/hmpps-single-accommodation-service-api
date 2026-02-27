package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.UUID

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
class OAuth2ResourceServerSecurityConfiguration {
  @Bean
  @Throws(Exception::class)
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      csrf { disable() }
      anonymous { disable() }
      oauth2ResourceServer {
        jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() }

        authenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
          response.apply {
            status = 401
            contentType = "application/problem+json"
            characterEncoding = "UTF-8"

            writer.write(
              """ {
                "title": "Unauthenticated",
                "status": 401,
                "detail": "A valid HMPPS Auth JWT must be supplied via bearer authentication to access this endpoint"
              }
              """.trimIndent(),
            )
          }
        }
      }

      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
    }

    return http.build()
  }
}

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val claims = jwt.claims
    val userUuid = claims[CLAIM_USER_UUID] as String
    val authSource = claims[CLAIM_AUTH_SOURCE] as String
    val principal = UserPrincipal(
      userUuid = UUID.fromString(userUuid),
      authSource = AuthSource.fromString(authSource),
      username = findPrincipal(claims)
    )
    val authorities = extractAuthorities(jwt)
    return AuthAwareAuthenticationToken(jwt, principal, authorities)
  }

  private fun findPrincipal(claims: Map<String, Any?>): String = if (claims.containsKey(CLAIM_USERNAME)) {
    claims[CLAIM_USERNAME] as String
  } else if (claims.containsKey(CLAIM_USER_ID)) {
    claims[CLAIM_USER_ID] as String
  } else if (claims.containsKey(CLAIM_CLIENT_ID)) {
    claims[CLAIM_CLIENT_ID] as String
  } else {
    throw RuntimeException("Unable to find a claim to identify Subject by")
  }

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
    val authorities = mutableListOf<GrantedAuthority>().apply { addAll(jwtGrantedAuthoritiesConverter.convert(jwt)) }
    if (jwt.claims.containsKey(CLAIM_AUTHORITY)) {
      @Suppress("UNCHECKED_CAST")
      val claimAuthorities = when (val claims = jwt.claims[CLAIM_AUTHORITY]) {
        is String -> claims.split(',')
        is Collection<*> -> (claims as Collection<String>).toList()
        else -> emptyList()
      }
      authorities.addAll(claimAuthorities.map(::SimpleGrantedAuthority))
    }
    return authorities.toSet()
  }

  companion object {
    const val CLAIM_USERNAME = "user_name"
    const val CLAIM_USER_UUID = "user_uuid"
    const val CLAIM_AUTH_SOURCE = "auth_source"
    const val CLAIM_USER_ID = "user_id"
    const val CLAIM_CLIENT_ID = "client_id"
    const val CLAIM_AUTHORITY = "authorities"
  }
}
