package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sas_case_refresh_request")
class CaseRefreshRequestEntity(
  @Id
  val caseId: UUID,
  var generation: Long,
  var processingGeneration: Long?,
  @Enumerated(EnumType.STRING)
  var status: CaseRefreshRequestStatus,
  @Enumerated(EnumType.STRING)
  val priority: CaseRefreshPriority,
  var requestedAt: Instant,
  var claimedAt: Instant?,
  var claimId: UUID?,
  var attemptCount: Int,
  var nextAttemptAt: Instant?,
  @Enumerated(EnumType.STRING)
  var lastFailureCategory: CaseRefreshFailureCategory?,
  var lastFailureDetail: String?,
  var failedAt: Instant?,
)

enum class CaseRefreshRequestStatus {
  PENDING,
  PROCESSING,
  FAILED,
}

enum class CaseRefreshPriority {
  LIVE,
  BULK,
}

enum class CaseRefreshFailureCategory {
  CURRENT_TIER_NOT_FOUND,
  UPSTREAM_CLIENT_ERROR,
  UPSTREAM_SERVER_ERROR,
  UPSTREAM_TIMEOUT,
  UPSTREAM_UNEXPECTED_ERROR,
  UNEXPECTED_ERROR,
}
