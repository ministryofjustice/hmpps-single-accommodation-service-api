package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.user

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserPreferenceEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserPreferenceRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserPreferencesQueryService

@ExtendWith(MockKExtension::class)
class UserPreferencesQueryServiceTest {
  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var userPreferenceRepository: UserPreferenceRepository

  private val user = buildUserEntity()

  private val service by lazy { UserPreferencesQueryService(userService, userPreferenceRepository) }

  @Test
  fun `tour is dismissed when the user has seen or skipped it`() {
    every { userService.authorizeAndRetrieveUser() } returns user
    every { userPreferenceRepository.findAllBySasUserId(user.id) } returns listOf(
      buildUserPreferenceEntity(
        sasUserId = user.id,
        preferenceName = UserPreferenceName.TOUR_DISMISSED,
        preferenceValue = "true",
      ),
    )

    assertThat(service.getUserPreferences().tourDismissed).isTrue()
  }

  @Test
  fun `tour is not dismissed when user has not seen or skipped it`() {
    every { userService.authorizeAndRetrieveUser() } returns user
    every { userPreferenceRepository.findAllBySasUserId(user.id) } returns emptyList()

    assertThat(service.getUserPreferences().tourDismissed).isFalse()
  }
}
