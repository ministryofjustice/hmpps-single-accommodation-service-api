package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCustomCaseListEntity
import java.util.UUID

@Repository
interface UserCustomCaseListRepository : JpaRepository<UserCustomCaseListEntity, UUID> {
  fun findAllBySasUserId(sasUserId: UUID): List<UserCustomCaseListEntity>

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

  @Query(
    nativeQuery = true,
    value = """
      SELECT latest.crn
      FROM (
        SELECT DISTINCT ON (sci.case_id) sci.identifier AS crn
        FROM sas_user_custom_case_list ucl
        JOIN sas_case_identifier sci ON sci.case_id = ucl.sas_case_id AND sci.identifier_type = 'CRN'
        WHERE ucl.sas_user_id = :sasUserId
        ORDER BY sci.case_id, sci.created_at DESC
      ) latest
      ORDER BY latest.crn
    """,
  )
  fun findLatestCrnsBySasUserId(sasUserId: UUID): List<String>
}
