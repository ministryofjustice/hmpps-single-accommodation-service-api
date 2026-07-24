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
        requested_at,
        attempt_count,
        next_attempt_at
      )
      VALUES (:caseId, 1, 'PENDING', :requestedAt, 0, :requestedAt)
      ON CONFLICT (case_id) DO UPDATE
      SET generation = case_refresh_request.generation + 1,
          status = CASE
              WHEN case_refresh_request.status = 'FAILED' THEN 'PENDING'
              ELSE case_refresh_request.status
          END,
          requested_at = EXCLUDED.requested_at,
          attempt_count = 0,
          next_attempt_at = EXCLUDED.next_attempt_at,
          last_failure_category = NULL,
          last_failure_detail = NULL,
          failed_at = NULL,
          processing_generation = CASE
              WHEN case_refresh_request.status = 'PROCESSING' THEN case_refresh_request.processing_generation
              ELSE NULL
          END,
          claimed_at = CASE
              WHEN case_refresh_request.status = 'PROCESSING' THEN case_refresh_request.claimed_at
              ELSE NULL
          END,
          claim_id = CASE
              WHEN case_refresh_request.status = 'PROCESSING' THEN case_refresh_request.claim_id
              ELSE NULL
          END
    """,
  )
  fun upsertPending(caseId: UUID, requestedAt: Instant)

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
      SELECT request
      FROM CaseRefreshRequestEntity request
      WHERE (
        request.status = :pendingStatus
        AND request.nextAttemptAt <= :now
      ) OR (
        request.status = :processingStatus
        AND request.claimedAt < :abandonedClaimedBefore
      )
      ORDER BY request.requestedAt
    """,
  )
  fun findClaimable(
    pendingStatus: CaseRefreshRequestStatus,
    processingStatus: CaseRefreshRequestStatus,
    now: Instant,
    abandonedClaimedBefore: Instant,
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
