package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralOutcomeReason as EntityOtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

object ExternalReferralTransformer {

  fun toExternalReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByName: String,
    createdByUsername: String,
  ) = ExternalReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName, createdByUsername),
  )

  fun toExternalReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByUser: UserEntity,
  ) = ExternalReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser),
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByName: String,
    createdByUsername: String,
  ) = ExternalReferralSubmissionDto(
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
  ) = ExternalReferralSubmissionDto(
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

  fun toStatus(status: EntityOtherAccommodationReferralStatus): ExternalReferralStatus = ExternalReferralStatus.valueOf(status.name)

  fun toOutcomeReason(outcomeReason: EntityOtherAccommodationReferralOutcomeReason?): ExternalReferralOutcomeReason? = outcomeReason?.let { ExternalReferralOutcomeReason.valueOf(it.name) }
}
