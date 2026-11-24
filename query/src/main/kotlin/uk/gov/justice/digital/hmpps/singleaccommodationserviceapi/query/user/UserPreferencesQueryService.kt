package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserPreferenceRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService

@Service
class UserPreferencesQueryService(
  private val userService: UserService,
  private val userPreferenceRepository: UserPreferenceRepository,
) {
  fun getUserPreferences(): UserPreferencesDto {
    val user = userService.authorizeAndRetrieveUser()
    val prefs = userPreferenceRepository.findAllBySasUserId(user.id)
      .associate { it.preferenceName to it.preferenceValue }
    return UserPreferencesDto(tourDismissed = prefs[UserPreferenceName.TOUR_DISMISSED].toBoolean())
  }
}

data class UserPreferencesDto(val tourDismissed: Boolean)
