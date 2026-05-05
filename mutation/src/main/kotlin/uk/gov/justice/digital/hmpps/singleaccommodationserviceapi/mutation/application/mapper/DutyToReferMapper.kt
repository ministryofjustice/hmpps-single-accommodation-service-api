package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.WithdrawalReason as EntityWithdrawalReason

object DutyToReferMapper {

  fun toEntity(snapshot: DutyToReferSnapshot) = DutyToReferEntity(
    id = snapshot.id,
    caseId = snapshot.caseId,
    localAuthorityAreaId = snapshot.localAuthorityAreaId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityDtrStatus.valueOf(snapshot.status.name),
    withdrawalReason = snapshot.withdrawalReason?.let { EntityWithdrawalReason.valueOf(it.name) },
    withdrawalReasonOther = snapshot.withdrawalReasonOther,
  )

  fun merge(snapshot: DutyToReferSnapshot, entity: DutyToReferEntity): DutyToReferEntity {
    entity.localAuthorityAreaId = snapshot.localAuthorityAreaId
    entity.referenceNumber = snapshot.referenceNumber
    entity.submissionDate = snapshot.submissionDate
    entity.status = EntityDtrStatus.valueOf(snapshot.status.name)
    entity.withdrawalReason = snapshot.withdrawalReason?.let { EntityWithdrawalReason.valueOf(it.name) }
    entity.withdrawalReasonOther = snapshot.withdrawalReasonOther
    entity.addMissingNotes(snapshot.notes)
    return entity
  }

  fun DutyToReferEntity.addMissingNotes(snapshotNotes: List<DutyToReferAggregate.DutyToReferNote>) {
    val existingIds = this.notes.map { it.id }.toSet()
    val missingNotes = snapshotNotes
      .filter { it.id !in existingIds }
      .map {
        DutyToReferNoteEntity(
          id = it.id,
          note = it.note,
          dutyToRefer = this,
        )
      }
    this.notes.addAll(missingNotes)
  }

  fun toAggregate(entity: DutyToReferEntity): DutyToReferAggregate = DutyToReferAggregate.hydrateExisting(
    id = entity.id,
    caseId = entity.caseId,
    localAuthorityAreaId = entity.localAuthorityAreaId,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    status = DtrStatus.valueOf(entity.status.name),
    notes = entity.notes.map {
      DutyToReferAggregate.DutyToReferNote(
        id = it.id,
        note = it.note,
      )
    },
    withdrawalReason = entity.withdrawalReason?.let { WithdrawalReason.valueOf(it.name) },
    withdrawalReasonOther = entity.withdrawalReasonOther,
  )

  fun toDto(
    snapshot: DutyToReferSnapshot,
    crn: String,
    createdBy: String,
    createdAt: Instant,
    localAuthorityAreaName: String,
  ) = DutyToReferDto(
    caseId = snapshot.caseId,
    crn = crn,
    status = DtrStatus.valueOf(snapshot.status.name),
    submission = DtrSubmissionDto(
      id = snapshot.id,
      localAuthority = LocalAuthorityDto(
        localAuthorityAreaId = snapshot.localAuthorityAreaId,
        localAuthorityAreaName = localAuthorityAreaName,
      ),
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      createdBy = createdBy,
      createdAt = createdAt,
      withdrawalReason = snapshot.withdrawalReason,
      withdrawalReasonOther = snapshot.withdrawalReasonOther,
    ),
  )
}
