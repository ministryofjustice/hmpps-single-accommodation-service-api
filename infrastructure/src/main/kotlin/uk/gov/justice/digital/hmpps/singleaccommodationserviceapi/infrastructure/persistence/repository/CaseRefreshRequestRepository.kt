package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import java.time.Instant
import java.util.UUID

interface CaseRefreshRequestRepository : JpaRepository<CaseRefreshRequestEntity, UUID> {

  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO case_refresh_request (
        case_id,
        generation,
        status,
        requested_at
      )
      VALUES (:caseId, 1, 'PENDING', :requestedAt)
      ON CONFLICT (case_id) DO UPDATE
      SET generation = case_refresh_request.generation + 1,
          requested_at = EXCLUDED.requested_at
    """,
  )
  fun upsertPending(caseId: UUID, requestedAt: Instant)

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
      SELECT request
      FROM CaseRefreshRequestEntity request
      WHERE request.status = :status
      ORDER BY request.requestedAt
    """,
  )
  fun findOldestByStatus(
    status: CaseRefreshRequestStatus,
    pageable: Pageable,
  ): List<CaseRefreshRequestEntity>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
      SELECT request
      FROM CaseRefreshRequestEntity request
      WHERE request.caseId = :caseId
    """,
  )
  fun findByCaseIdForUpdate(caseId: UUID): CaseRefreshRequestEntity?
}
