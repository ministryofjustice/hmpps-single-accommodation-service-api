package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
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
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName, createdByUsername, localAuthorityAreaName),
  )

  fun toOtherAccommodationReferralDto(
    entity: OtherAccommodationReferralEntity,
    crn: String,
    createdByUser: UserEntity,
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser, localAuthorityAreaName),
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByName: String,
    createdByUsername: String,
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralSubmissionDto(
    id = entity.id,
    localAuthority = toLocalAuthority(entity, localAuthorityAreaName),
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByName,
    createdByUsername = createdByUsername,
    createdAt = entity.createdAt!!,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    outcomeReason = toOutcomeReason(entity.outcomeReason),
    outcomeNote = entity.outcomeNote,
  )

  fun toSubmission(
    entity: OtherAccommodationReferralEntity,
    createdByUser: UserEntity,
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralSubmissionDto(
    id = entity.id,
    localAuthority = toLocalAuthority(entity, localAuthorityAreaName),
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByUser.displayName(),
    createdByUsername = createdByUser.username,
    createdAt = entity.createdAt!!,
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    outcomeReason = toOutcomeReason(entity.outcomeReason),
    outcomeNote = entity.outcomeNote,
  )

  fun toLocalAuthority(entity: OtherAccommodationReferralEntity, localAuthorityAreaName: String?) = LocalAuthorityDto(
    localAuthorityAreaId = entity.localAuthorityAreaId,
    localAuthorityAreaName = localAuthorityAreaName,
  )

  fun toStatus(status: EntityOtherAccommodationReferralStatus): OtherAccommodationReferralStatus = OtherAccommodationReferralStatus.valueOf(status.name)

  fun toOutcomeReason(outcomeReason: EntityOtherAccommodationReferralOutcomeReason?): OtherAccommodationReferralOutcomeReason? = outcomeReason?.let { OtherAccommodationReferralOutcomeReason.valueOf(it.name) }
}
