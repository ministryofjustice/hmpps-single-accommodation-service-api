package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCaseListEntryEntity
import java.time.Instant
import java.util.UUID

@Repository
interface UserCaseListEntryRepository : JpaRepository<UserCaseListEntryEntity, UUID> {
  fun findAllBySasUserIdAndListType(sasUserId: UUID, listType: CaseListType): List<UserCaseListEntryEntity>

  @Modifying
  @Query("delete from UserCaseListEntryEntity e where e.sasUserId = :sasUserId and e.listType = :listType")
  fun deleteBySasUserIdAndListType(sasUserId: UUID, listType: CaseListType)

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_user_case_list_entry (id, sas_user_id, sas_case_id, list_type)
      SELECT gen_random_uuid(), :sasUserId, case_id, :#{#listType.name()}
      FROM unnest(cast(:caseIds as uuid[])) AS case_id
    """,
  )
  fun insertAll(sasUserId: UUID, listType: CaseListType, caseIds: Array<UUID>)

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_user_case_list_entry (id, sas_user_id, sas_case_id, list_type, created_at, last_updated_at)
      VALUES (gen_random_uuid(), :sasUserId, :caseId, :#{#listType.name()}, :timestamp, :timestamp)
      ON CONFLICT (sas_user_id, list_type, sas_case_id) DO UPDATE SET last_updated_at = EXCLUDED.last_updated_at
    """,
  )
  fun insertOrUpdate(sasUserId: UUID, listType: CaseListType, caseId: UUID, timestamp: Instant)

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      DELETE FROM sas_user_case_list_entry
      WHERE id IN (
        SELECT id
        FROM sas_user_case_list_entry
        WHERE sas_user_id = :sasUserId AND list_type = :#{#listType.name()}
        ORDER BY last_updated_at DESC
        OFFSET :maxEntries
      )
    """,
  )
  fun deleteOldestBeyondLimit(sasUserId: UUID, listType: CaseListType, maxEntries: Int)

  @Query(
    nativeQuery = true,
    value = """
      SELECT DISTINCT ON (sci.case_id) sci.identifier AS "crn", ucl.last_updated_at AS "lastUpdatedAt"
      FROM sas_user_case_list_entry ucl
      JOIN sas_case_identifier sci ON sci.case_id = ucl.sas_case_id AND sci.identifier_type = 'CRN'
      JOIN sas_case sc ON sc.id = ucl.sas_case_id
      WHERE ucl.sas_user_id = :sasUserId AND ucl.list_type = :#{#listType.name()} AND sc.archived = false
      ORDER BY sci.case_id, sci.created_at DESC
    """,
  )
  fun findLatestCrnsBySasUserIdAndListType(sasUserId: UUID, listType: CaseListType): List<UserCaseListCrn>
}

interface UserCaseListCrn {
  val crn: String
  val lastUpdatedAt: Instant
}
