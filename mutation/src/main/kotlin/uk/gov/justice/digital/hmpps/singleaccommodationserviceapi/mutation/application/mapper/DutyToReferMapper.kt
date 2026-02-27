package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrOutcomeStatus as EntityDtrOutcomeStatus

object DutyToReferMapper {

  fun toEntity(snapshot: DutyToReferSnapshot) = DutyToReferEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    localAuthorityAreaId = snapshot.localAuthorityAreaId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    outcomeStatus = snapshot.outcomeStatus?.let { EntityDtrOutcomeStatus.valueOf(it.name) },
    outcomeDate = snapshot.outcomeDate,
  )

  fun toDto(
    snapshot: DutyToReferSnapshot,
    createdBy: String,
    createdAt: Instant,
  ) = DutyToReferDto(
    crn = snapshot.crn,
    serviceStatus = DtrServiceStatus.SUBMITTED,
    action = null,
    submission = DtrSubmissionDto(
      id = snapshot.id,
      localAuthorityAreaId = snapshot.localAuthorityAreaId,
      localAuthorityAreaName = null,
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      outcomeStatus = snapshot.outcomeStatus,
      outcomeDate = snapshot.outcomeDate,
      createdBy = createdBy,
      createdAt = createdAt,
    ),
  )
}
