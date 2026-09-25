package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralOutcomeReason as EntityOtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

object OtherAccommodationReferralTransformer {

  fun toOtherAccommodationReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByName: String,
    createdByUsername: String,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName, createdByUsername),
  )

  fun toOtherAccommodationReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByUser: UserEntity,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser),
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByName: String,
    createdByUsername: String,
  ) = OtherAccommodationReferralSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByName,
    createdByUsername = createdByUsername,
    createdAt = entity.createdAt!!,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    email = entity.email,
    phoneNumber = entity.phoneNumber,
    outcomeReason = toOutcomeReason(entity.outcomeReason),
    outcomeNote = entity.outcomeNote,
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByUser: UserEntity,
  ) = OtherAccommodationReferralSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByUser.displayName(),
    createdByUsername = createdByUser.username,
    createdAt = entity.createdAt!!,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    email = entity.email,
    phoneNumber = entity.phoneNumber,
    outcomeReason = toOutcomeReason(entity.outcomeReason),
    outcomeNote = entity.outcomeNote,
  )

  fun toStatus(status: EntityOtherAccommodationReferralStatus): OtherAccommodationReferralStatus = OtherAccommodationReferralStatus.valueOf(status.name)

  fun toOutcomeReason(outcomeReason: EntityOtherAccommodationReferralOutcomeReason?): OtherAccommodationReferralOutcomeReason? = outcomeReason?.let { OtherAccommodationReferralOutcomeReason.valueOf(it.name) }
}
