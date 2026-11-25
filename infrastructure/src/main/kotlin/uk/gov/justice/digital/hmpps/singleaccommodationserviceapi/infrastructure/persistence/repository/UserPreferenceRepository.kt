package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserPreferenceEntity
import java.time.Instant
import java.util.UUID

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreferenceEntity, UUID> {
  fun findAllBySasUserId(sasUserId: UUID): List<UserPreferenceEntity>

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_user_preference (id, sas_user_id, name, value, updated_at)
      VALUES (gen_random_uuid(), :sasUserId, :#{#name.name()}, :value, :timestamp)
      ON CONFLICT (sas_user_id, name)
      DO UPDATE SET value = EXCLUDED.value, updated_at = EXCLUDED.updated_at
    """,
  )
  fun upsert(sasUserId: UUID, name: UserPreference, value: String, timestamp: Instant)
}
