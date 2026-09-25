package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "onboarded_team")
class OnboardedTeamEntity(
  @Id
  val teamCode: String,
  val onboardedAt: Instant = Instant.now(),
)
