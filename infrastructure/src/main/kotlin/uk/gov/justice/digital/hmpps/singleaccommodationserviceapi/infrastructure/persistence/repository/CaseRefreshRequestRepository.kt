package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import java.time.Instant
import java.util.UUID

interface CaseRefreshRequestRepository : JpaRepository<CaseRefreshRequestEntity, UUID> {

  // Atomic upsert used by live triggers.
  // Invariants:
  // - Always bump generation when a trigger arrives.
  // - Promote to LIVE priority.
  // - Reopen FAILED work to PENDING.
  // - Preserve active claim metadata only while PROCESSING.
  // - Preserve the most restrictive timing window by keeping earliest requested_at and latest next_attempt_at.
  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_case_refresh_request (
        case_id,
        generation,
        status,
        priority,
        requested_at,
        attempt_count,
        next_attempt_at
      )
      VALUES (:caseId, 1, 'PENDING', :#{#priority.name()}, :requestedAt, 0, :requestedAt)
      ON CONFLICT (case_id) DO UPDATE
      SET generation = sas_case_refresh_request.generation + 1,
          status = CASE
              WHEN sas_case_refresh_request.status = 'FAILED' THEN 'PENDING'
              ELSE sas_case_refresh_request.status
          END,
          priority = :#{#priority.name()},
          requested_at = LEAST(sas_case_refresh_request.requested_at, EXCLUDED.requested_at),
          next_attempt_at = GREATEST(COALESCE(sas_case_refresh_request.next_attempt_at, EXCLUDED.next_attempt_at), EXCLUDED.next_attempt_at),
          failed_at = NULL,
          processing_generation = CASE
              WHEN sas_case_refresh_request.status = 'PROCESSING' THEN sas_case_refresh_request.processing_generation
              ELSE NULL
          END,
          claimed_at = CASE
              WHEN sas_case_refresh_request.status = 'PROCESSING' THEN sas_case_refresh_request.claimed_at
              ELSE NULL
          END,
          claim_id = CASE
              WHEN sas_case_refresh_request.status = 'PROCESSING' THEN sas_case_refresh_request.claim_id
              ELSE NULL
          END
    """,
  )
  fun upsertRequest(caseId: UUID, priority: CaseRefreshPriority, requestedAt: Instant)

  // bulk refresh for a case that does not have a live request already
  @Modifying
  @Query(
    nativeQuery = true,
    value = """
      INSERT INTO sas_case_refresh_request (
        case_id,
        generation,
        status,
        priority,
        requested_at,
        attempt_count,
        next_attempt_at
      )
      SELECT case_id, 1, 'PENDING', :#{#priority.name()}, :requestedAt, 0, :requestedAt
      FROM unnest(cast(:caseIds as uuid[])) AS case_id
      ON CONFLICT (case_id) DO NOTHING
    """,
  )
  fun insertBulkRequests(caseIds: Array<UUID>, priority: CaseRefreshPriority, requestedAt: Instant)

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
      ORDER BY CASE WHEN request.priority = :livePriority THEN 0 ELSE 1 END, request.requestedAt
    """,
  )
  fun findClaimable(
    pendingStatus: CaseRefreshRequestStatus,
    processingStatus: CaseRefreshRequestStatus,
    livePriority: CaseRefreshPriority,
    now: Instant,
    abandonedClaimedBefore: Instant,
    pageable: Pageable,
  ): List<CaseRefreshRequestEntity>

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  fun findByCaseId(caseId: UUID): CaseRefreshRequestEntity?
}
