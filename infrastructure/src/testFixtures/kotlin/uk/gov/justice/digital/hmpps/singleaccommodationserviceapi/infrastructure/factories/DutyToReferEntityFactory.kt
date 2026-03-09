package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: DtrStatus = DtrStatus.SUBMITTED,
  createdByUserId: UUID? = null,
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID? = null,
  lastUpdatedAt: Instant = Instant.now(),
) = DutyToReferEntity(
  id = id,
  crn = crn,
  localAuthorityAreaId = localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId
  this.lastUpdatedAt = lastUpdatedAt
}
