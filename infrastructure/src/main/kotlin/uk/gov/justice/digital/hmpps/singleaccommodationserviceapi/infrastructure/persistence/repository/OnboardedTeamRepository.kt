package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OnboardedTeamEntity

interface OnboardedTeamRepository : JpaRepository<OnboardedTeamEntity, String> {

  @Modifying
  @Transactional
  @Query(
    nativeQuery = true,
    value = "INSERT INTO onboarded_team (team_code) VALUES (:teamCode) ON CONFLICT (team_code) DO NOTHING",
  )
  fun createOnboardedTeam(teamCode: String)
}
