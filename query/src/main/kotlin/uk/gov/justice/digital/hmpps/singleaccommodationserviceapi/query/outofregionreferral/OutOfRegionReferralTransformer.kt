package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.outofregionreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus

object OutOfRegionReferralTransformer {

  fun toOutOfRegionReferralDto(
    entity: OutOfRegionReferralEntity,
    crn: String,
    createdByName: String,
    active: Boolean? = null,
  ) = OutOfRegionReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName),
    active = active,
  )

  fun toOutOfRegionReferralDto(
    entity: OutOfRegionReferralEntity,
    crn: String,
    createdByUser: UserEntity,
    active: Boolean? = null,
  ) = OutOfRegionReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser),
    active = active,
  )

  fun toSubmission(
    entity: OutOfRegionReferralEntity,
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
    entity: OutOfRegionReferralEntity,
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
