package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

object DutyToReferTransformer {

  fun toNotStartedDto(crn: String) = DutyToReferDto(
    crn = crn,
    status = DtrStatus.NOT_STARTED,
    submission = null,
  )

  fun toDutyToReferDto(
    entity: DutyToReferEntity,
    createdByName: String,
    localAuthorityAreaName: String?,
  ) = DutyToReferDto(
    crn = entity.crn,
    status = toStatus(entity.status),
    submission = toSubmission(entity, createdByName, localAuthorityAreaName),
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
  )

  fun toLocalAuthority(entity: DutyToReferEntity, localAuthorityAreaName: String?) = LocalAuthorityDto(
    localAuthorityAreaId = entity.localAuthorityAreaId,
    localAuthorityAreaName = localAuthorityAreaName,
  )

  fun toStatus(status: EntityDtrStatus): DtrStatus = DtrStatus.valueOf(status.name)
}
