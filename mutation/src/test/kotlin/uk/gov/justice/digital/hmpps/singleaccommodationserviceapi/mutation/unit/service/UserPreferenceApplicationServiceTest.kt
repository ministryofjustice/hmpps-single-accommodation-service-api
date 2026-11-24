package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateUserPreferencesCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserPreferenceRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.UserPreferenceApplicationService
import java.time.Clock
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UserPreferenceApplicationServiceTest {

  @RelaxedMockK
  private lateinit var userPreferenceRepository: UserPreferenceRepository

  @MockK
  private lateinit var clock: Clock

  @InjectMockKs
  private lateinit var userPreferenceApplicationService: UserPreferenceApplicationService

  private val userId = UUID.randomUUID()
  private val now = Instant.parse("2026-11-24T12:34:56Z")

  @Test
  fun `upserts the tour dismissed preference when it is supplied`() {
    every { clock.instant() } returns now

    userPreferenceApplicationService.updatePreferences(userId, UpdateUserPreferencesCommand(tourDismissed = true))

    verify(exactly = 1) {
      userPreferenceRepository.upsert(userId, UserPreferenceName.TOUR_DISMISSED, "true", now)
    }
  }

  @Test
  fun `upserts false when the tour dismissed preference is set to false`() {
    every { clock.instant() } returns now

    userPreferenceApplicationService.updatePreferences(userId, UpdateUserPreferencesCommand(tourDismissed = false))

    verify(exactly = 1) {
      userPreferenceRepository.upsert(userId, UserPreferenceName.TOUR_DISMISSED, "false", now)
    }
  }

  @Test
  fun `does not change any preferences when none are supplied`() {
    userPreferenceApplicationService.updatePreferences(userId, UpdateUserPreferencesCommand())

    verify(exactly = 0) { userPreferenceRepository.upsert(any(), any(), any(), any()) }
  }
}
