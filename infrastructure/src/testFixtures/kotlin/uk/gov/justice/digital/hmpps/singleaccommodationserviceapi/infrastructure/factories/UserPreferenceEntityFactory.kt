package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceName
import java.time.Instant
import java.util.UUID

fun buildUserPreferenceEntity(
  id: UUID = UUID.randomUUID(),
  sasUserId: UUID = UUID.randomUUID(),
  preferenceName: UserPreferenceName = UserPreferenceName.TOUR_DISMISSED,
  preferenceValue: String = "true",
  updatedAt: Instant = Instant.now(),
) = UserPreferenceEntity(
  id = id,
  sasUserId = sasUserId,
  preferenceName = preferenceName,
  preferenceValue = preferenceValue,
  updatedAt = updatedAt,
)
