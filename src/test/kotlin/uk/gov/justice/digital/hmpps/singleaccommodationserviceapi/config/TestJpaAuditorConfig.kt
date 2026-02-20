package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.AuditorAware
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import java.util.*

@TestConfiguration
class TestJpaAuditorConfig(
  private val userService: UserService,
  @Value($$"${test-data-setup.user-id}")
  private val testDataSetupUserId: UUID,
) {

  @Primary
  @Bean
  fun auditorAware(): AuditorAware<UUID> = AuditorAware {
    try {
      val deliusUser = userService.getDeliusUserForRequest()
      Optional.of(deliusUser.id)
    } catch (_: Exception) {
      Optional.of(testDataSetupUserId)
    }
  }
}
