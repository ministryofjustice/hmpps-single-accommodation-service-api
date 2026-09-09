package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus

object OtherAccommodationReferralTransformer {

  fun toOtherAccommodationReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByName: String,
    active: Boolean? = null,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName),
    active = active,
  )

  fun toOtherAccommodationReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByUser: UserEntity,
    active: Boolean? = null,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser),
    active = active,
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByName: String,
  ) = OorSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByName,
    createdAt = entity.createdAt!!,
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
    submissionNote = entity.submissionNote,
    outcomeNote = entity.outcomeNote,
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByUser: UserEntity,
  ) = OorSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByUser.displayName(),
    createdByUsername = createdByUser.username,
    createdAt = entity.createdAt!!,
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
  )

  fun toStatus(status: EntityOorStatus): OorStatus = OorStatus.valueOf(status.name)
}
