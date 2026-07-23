package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class CaseRefreshRequestService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val clock: Clock,
) {

  @Transactional
  fun requestLiveRefresh(crn: String): Result {
    val caseEntity = caseRepository.findByCrn(crn) ?: return Result.CASE_NOT_FOUND
    caseRefreshRequestRepository.upsertPending(caseEntity.id, Instant.now(clock))
    return Result.REQUESTED
  }

  @Transactional
  fun claimPending(maxRequests: Int): List<Claim> {
    val claimedAt = Instant.now(clock)
    return caseRefreshRequestRepository.findOldestByStatus(
      CaseRefreshRequestStatus.PENDING,
      PageRequest.of(0, maxRequests),
    ).map { request ->
      request.status = CaseRefreshRequestStatus.PROCESSING
      request.processingGeneration = request.generation
      request.claimedAt = claimedAt
      Claim(request.caseId, request.generation)
    }
  }

  @Transactional
  fun returnToPending(claim: Claim) {
    val request = caseRefreshRequestRepository.findByCaseIdForUpdate(claim.caseId) ?: return
    if (request.processingGeneration != claim.generation) {
      return
    }
    request.returnToPending()
  }

  data class Claim(
    val caseId: UUID,
    val generation: Long,
  )

  enum class Result {
    REQUESTED,
    CASE_NOT_FOUND,
  }
}
