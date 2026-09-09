package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildOtherAccommodationReferralEntity(
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
) = OtherAccommodationReferralEntity(
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
fun buildOtherAccommodationReferralNoteEntity(
  id: UUID = UUID.randomUUID(),
  note: String = "Test note",
  createdByUserId: UUID? = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  otherAccommodationReferralEntity: OtherAccommodationReferralEntity,
) = OtherAccommodationReferralNoteEntity(
  id,
  note,
  otherAccommodationReferralEntity,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
}
