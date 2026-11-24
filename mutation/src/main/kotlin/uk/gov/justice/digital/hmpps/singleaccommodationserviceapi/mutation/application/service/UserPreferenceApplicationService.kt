package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateUserPreferencesCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserPreferenceRepository
import java.time.Clock
import java.util.UUID

@Service
class UserPreferenceApplicationService(
  private val userPreferenceRepository: UserPreferenceRepository,
  private val clock: Clock,
) {
  @Transactional
  fun updatePreferences(userId: UUID, command: UpdateUserPreferencesCommand) {
    command.tourDismissed?.let {
      userPreferenceRepository.upsert(userId, UserPreferenceName.TOUR_DISMISSED, it.toString(), clock.instant())
    }
  }
}
