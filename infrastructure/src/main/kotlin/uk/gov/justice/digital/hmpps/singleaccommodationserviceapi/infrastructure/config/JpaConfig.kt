package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.Optional
import java.util.UUID

@Configuration
@EnableJpaAuditing
class JpaConfig

@Configuration
class JpaAuditorConfig {

  @Bean
  @Profile("local", "dev", "preprod", "prod")
  fun auditorAware(): AuditorAware<UUID> = AuditorAware {
    val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    val userPrincipal = authToken.principal as UserPrincipal
    Optional.of(userPrincipal.sasUserId)
  }
}
