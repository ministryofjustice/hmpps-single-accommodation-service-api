package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sas_user_preference")
class UserPreferenceEntity(
  @Id val id: UUID = UUID.randomUUID(),
  val sasUserId: UUID,
  @Enumerated(EnumType.STRING) val preferenceName: UserPreferenceName,
  val preferenceValue: String,
  val updatedAt: Instant = Instant.now(),
)

enum class UserPreferenceName { TOUR_DISMISSED }
