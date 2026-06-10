package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

object DutyToReferTransformer {

  fun toDutyToReferDto(
    entity: DutyToReferEntity,
    crn: String,
    createdByName: String,
    localAuthorityAreaName: String?,
  ) = DutyToReferDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName, localAuthorityAreaName),
  )

  fun toDutyToReferDto(
    entity: DutyToReferEntity,
    crn: String,
    createdByUser: UserEntity,
    localAuthorityAreaName: String?,
  ) = DutyToReferDto(
    caseId = entity.caseId,
    crn = crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByUser, localAuthorityAreaName),
  )

  fun toSubmission(
    entity: DutyToReferEntity,
    createdByName: String,
    localAuthorityAreaName: String?,
  ) = DtrSubmissionDto(
    id = entity.id,
    localAuthority = toLocalAuthority(entity, localAuthorityAreaName),
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByName,
    createdAt = entity.createdAt!!,
    withdrawalReason = entity.withdrawalReason?.let { WithdrawalReason.valueOf(it.name) },
    withdrawalReasonOther = entity.withdrawalReasonOther,
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
  )

  fun toSubmission(
    entity: DutyToReferEntity,
    createdByUser: UserEntity,
    localAuthorityAreaName: String?,
  ) = DtrSubmissionDto(
    id = entity.id,
    localAuthority = toLocalAuthority(entity, localAuthorityAreaName),
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    createdBy = createdByUser.displayName(),
    createdByUsername = createdByUser.username,
    createdAt = entity.createdAt!!,
    withdrawalReason = entity.withdrawalReason?.let { WithdrawalReason.valueOf(it.name) },
    withdrawalReasonOther = entity.withdrawalReasonOther,
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
  )

  fun toLocalAuthority(entity: DutyToReferEntity, localAuthorityAreaName: String?) = LocalAuthorityDto(
    localAuthorityAreaId = entity.localAuthorityAreaId,
    localAuthorityAreaName = localAuthorityAreaName,
  )

  fun toStatus(status: EntityDtrStatus): DtrStatus = DtrStatus.valueOf(status.name)
}
