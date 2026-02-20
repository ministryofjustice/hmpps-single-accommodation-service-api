package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import java.util.Optional
import java.util.UUID

@Configuration
@EnableJpaAuditing
class JpaConfig

@Configuration
class JpaAuditorConfig(
  private val userService: UserService,
) {

  @Bean
  @Profile("dev", "preprod", "prod")
  fun auditorAware(): AuditorAware<UUID> =
    AuditorAware {
      val deliusUser = userService.getDeliusUserForRequest()
      Optional.of(deliusUser.id)
    }
}