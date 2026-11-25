package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceEntity
import java.time.Instant
import java.util.UUID

fun buildUserPreferenceEntity(
  id: UUID = UUID.randomUUID(),
  sasUserId: UUID = UUID.randomUUID(),
  name: UserPreference = UserPreference.TOUR_DISMISSED,
  value: String = "true",
  updatedAt: Instant = Instant.now(),
) = UserPreferenceEntity(
  id = id,
  sasUserId = sasUserId,
  name = name,
  value = value,
  updatedAt = updatedAt,
)
