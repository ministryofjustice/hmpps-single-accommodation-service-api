package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.externalreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus as EntityExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason as EntityExternalReferralWithdrawalReason

object ExternalReferralTransformer {

  fun toExternalReferralDto(
    entity: ExternalReferralEntity,
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
    entity: ExternalReferralEntity,
    crn: String,
    createdByUser: UserEntity,
  ) = ExternalReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser),
  )

  fun toSubmission(
    entity: ExternalReferralEntity,
    createdByName: String,
    createdByUsername: String,
  ) = ExternalReferralSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByName,
    createdByUsername = createdByUsername,
    createdAt = entity.createdAt,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    email = entity.email,
    phoneNumber = entity.phoneNumber,
    outcomeNote = entity.outcomeNote,
  )

  fun toSubmission(
    entity: ExternalReferralEntity,
    createdByUser: UserEntity,
  ) = ExternalReferralSubmissionDto(
    id = entity.id,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByUser.displayName(),
    createdByUsername = createdByUser.username,
    createdAt = entity.createdAt,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    email = entity.email,
    phoneNumber = entity.phoneNumber,
    outcomeNote = entity.outcomeNote,
  )

  fun toStatus(status: EntityExternalReferralStatus): ExternalReferralStatus = ExternalReferralStatus.valueOf(status.name)

  fun toWithdrawalReason(withdrawalReason: EntityExternalReferralWithdrawalReason?): ExternalReferralWithdrawalReason? = withdrawalReason?.let { ExternalReferralWithdrawalReason.valueOf(it.name) }
}
