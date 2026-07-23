package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "case_refresh_request")
class CaseRefreshRequestEntity(
  @Id
  val caseId: UUID,
  var generation: Long,
  var processingGeneration: Long?,
  @Enumerated(EnumType.STRING)
  var status: CaseRefreshRequestStatus,
  var requestedAt: Instant,
  var claimedAt: Instant?,
) {
  fun returnToPending() {
    status = CaseRefreshRequestStatus.PENDING
    processingGeneration = null
    claimedAt = null
  }
}

enum class CaseRefreshRequestStatus {
  PENDING,
  PROCESSING,
}
