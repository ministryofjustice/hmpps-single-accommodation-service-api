package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCustomCaseListEntity
import java.util.UUID

@Repository
interface UserCustomCaseListRepository : JpaRepository<UserCustomCaseListEntity, UUID> {
  @Modifying
  @Query("delete from UserCustomCaseListEntity e where e.sasUserId = :sasUserId")
  fun deleteBySasUserId(sasUserId: UUID)

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_user_custom_case_list (id, sas_user_id, sas_case_id)
      SELECT gen_random_uuid(), :sasUserId, case_id
      FROM unnest(cast(:caseIds as uuid[])) AS case_id
    """,
  )
  fun insertAll(sasUserId: UUID, caseIds: Array<UUID>)
}
