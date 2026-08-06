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
      VALUES (:caseId, 1, 'PENDING', 'LIVE', :requestedAt, 0, :requestedAt)
      ON CONFLICT (case_id) DO UPDATE
      SET generation = sas_case_refresh_request.generation + 1,
          status = CASE
              WHEN sas_case_refresh_request.status = 'FAILED' THEN 'PENDING'
              ELSE sas_case_refresh_request.status
          END,
          priority = 'LIVE',
          requested_at = LEAST(sas_case_refresh_request.requested_at, EXCLUDED.requested_at),
          next_attempt_at = GREATEST(sas_case_refresh_request.next_attempt_at, EXCLUDED.next_attempt_at),
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
  fun upsertLiveRequest(caseId: UUID, requestedAt: Instant)

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
      SELECT case_id, 1, 'PENDING', 'BULK', :requestedAt, 0, :requestedAt
      FROM unnest((:caseIds)::uuid[]) AS case_id
      ON CONFLICT (case_id) DO NOTHING
    """,
  )
  fun insertBulkRequests(caseIds: Array<String>, requestedAt: Instant)

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
  @Query(
    """
      SELECT request
      FROM CaseRefreshRequestEntity request
      WHERE request.caseId = :caseId
    """,
  )
  fun findByCaseIdForUpdate(caseId: UUID): CaseRefreshRequestEntity?
}
