package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import java.util.Optional
import java.util.UUID

@Configuration
@EnableJpaAuditing
class JpaConfig(
  private val httpAuthService: HttpAuthService,
  private val userRepository: UserRepository
) {

  @Bean
  fun auditorAware(): AuditorAware<UUID> =
    AuditorAware {
      val username = httpAuthService.getDeliusPrincipalOrThrow().name
      userRepository.findIdByUsername(username)
        ?.let { Optional.of(it) }
        ?: Optional.empty()
    }
}