package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildOutOfRegionReferralEntity(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "OOR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: OorStatus = OorStatus.SUBMITTED,
  outcomeReason: OutcomeReason? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
  createdByUserId: UUID? = null,
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID? = null,
  lastUpdatedAt: Instant = Instant.now(),
) = OutOfRegionReferralEntity(
  id = id,
  caseId = caseId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  outcomeReason = outcomeReason,
  submissionNote = submissionNote,
  outcomeNote = outcomeNote,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId
  this.lastUpdatedAt = lastUpdatedAt
}

@TestData
fun buildOutOfRegionReferralNoteEntity(
  id: UUID = UUID.randomUUID(),
  note: String = "Test note",
  createdByUserId: UUID? = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  outOfRegionReferralEntity: OutOfRegionReferralEntity,
) = OutOfRegionReferralNoteEntity(
  id,
  note,
  outOfRegionReferralEntity,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
}
