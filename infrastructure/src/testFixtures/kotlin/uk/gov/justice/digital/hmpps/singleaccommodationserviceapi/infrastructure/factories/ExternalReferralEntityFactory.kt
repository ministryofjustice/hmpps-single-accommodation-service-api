package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildExternalReferralEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  caseId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "OA-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: ExternalReferralStatus = ExternalReferralStatus.SUBMITTED,
  organisationName: String? = null,
  website: String? = null,
  submissionNote: String? = null,
  email: String? = null,
  phoneNumber: String? = null,
  withdrawalReason: ExternalReferralWithdrawalReason? = null,
  withdrawalNote: String? = null,
  outcomeNote: String? = null,
  createdByUserId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID = UUID.randomUUID(),
  lastUpdatedAt: Instant = Instant.now(),
) = ExternalReferralEntity(
  id = id,
  crn = crn,
  caseId = caseId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  email = email,
  phoneNumber = phoneNumber,
  withdrawalReason = withdrawalReason,
  withdrawalNote = withdrawalNote,
  outcomeNote = outcomeNote,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId
  this.lastUpdatedAt = lastUpdatedAt
}

@TestData
fun buildExternalReferralNoteEntity(
  id: UUID = UUID.randomUUID(),
  note: String = "Test note",
  createdByUserId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID = UUID.randomUUID(),
  lastUpdatedAt: Instant = Instant.now(),
  externalReferralEntity: ExternalReferralEntity,
) = ExternalReferralNoteEntity(
  id,
  note,
  externalReferralEntity,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId
  this.lastUpdatedAt = lastUpdatedAt
}
