package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildOtherAccommodationReferralEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  caseId: UUID = UUID.randomUUID(),
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  localAuthorityArea: LocalAuthorityAreaEntity? = null,
  referenceNumber: String? = "OA-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: OtherAccommodationReferralStatus = OtherAccommodationReferralStatus.SUBMITTED,
  organisationName: String? = null,
  website: String? = null,
  submissionNote: String? = null,
  createdByUser: UserEntity? = null,
  lastUpdatedByUser: UserEntity? = null,
  createdByUserId: UUID? = null,
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID? = null,
  lastUpdatedAt: Instant = Instant.now(),
) = OtherAccommodationReferralEntity(
  id = id,
  crn = crn,
  caseId = caseId,
  localAuthorityAreaId = localAuthorityArea?.id ?: localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  createdByUser = createdByUser,
  lastUpdatedByUser = lastUpdatedByUser,
  localAuthorityArea = localAuthorityArea,
).apply {
  this.createdByUserId = createdByUserId ?: createdByUser?.id
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId ?: lastUpdatedByUser?.id
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
