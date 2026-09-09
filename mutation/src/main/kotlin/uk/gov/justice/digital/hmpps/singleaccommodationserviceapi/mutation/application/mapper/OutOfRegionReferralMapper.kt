package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OutOfRegionReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OutOfRegionReferralAggregate.OutOfRegionReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason as EntityOutcomeReason

object OutOfRegionReferralMapper {

  fun toEntity(snapshot: OutOfRegionReferralSnapshot) = OutOfRegionReferralEntity(
    id = snapshot.id,
    caseId = snapshot.caseId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityOorStatus.valueOf(snapshot.status.name),
    outcomeReason = snapshot.outcomeReason?.let { EntityOutcomeReason.valueOf(it.name) },
    submissionNote = snapshot.submissionNote,
    outcomeNote = snapshot.outcomeNote,
  )

  fun merge(snapshot: OutOfRegionReferralSnapshot, entity: OutOfRegionReferralEntity): OutOfRegionReferralEntity {
    entity.referenceNumber = snapshot.referenceNumber
    entity.submissionDate = snapshot.submissionDate
    entity.status = EntityOorStatus.valueOf(snapshot.status.name)
    entity.outcomeReason = snapshot.outcomeReason?.let { EntityOutcomeReason.valueOf(it.name) }
    entity.submissionNote = snapshot.submissionNote
    entity.outcomeNote = snapshot.outcomeNote
    entity.addMissingNotes(snapshot.notes)
    return entity
  }

  fun OutOfRegionReferralEntity.addMissingNotes(snapshotNotes: List<OutOfRegionReferralAggregate.OutOfRegionReferralNote>) {
    val existingIds = this.notes.map { it.id }.toSet()
    val missingNotes = snapshotNotes
      .filter { it.id !in existingIds }
      .map {
        OutOfRegionReferralNoteEntity(
          id = it.id,
          note = it.note,
          outOfRegionReferral = this,
        )
      }
    this.notes.addAll(missingNotes)
  }

  fun toAggregate(entity: OutOfRegionReferralEntity): OutOfRegionReferralAggregate = OutOfRegionReferralAggregate.hydrateExisting(
    id = entity.id,
    caseId = entity.caseId,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    status = OorStatus.valueOf(entity.status.name),
    notes = entity.notes.map {
      OutOfRegionReferralAggregate.OutOfRegionReferralNote(
        id = it.id,
        note = it.note,
      )
    },
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
    submissionNote = entity.submissionNote,
    outcomeNote = entity.outcomeNote,
  )

  fun toDto(
    snapshot: OutOfRegionReferralSnapshot,
    crn: String,
    createdBy: String,
    createdAt: Instant,
  ) = OutOfRegionReferralDto(
    caseId = snapshot.caseId,
    crn = crn,
    status = OorStatus.valueOf(snapshot.status.name),
    submission = OorSubmissionDto(
      id = snapshot.id,
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      createdBy = createdBy,
      createdAt = createdAt,
      outcomeReason = snapshot.outcomeReason,
      submissionNote = snapshot.submissionNote,
      outcomeNote = snapshot.outcomeNote,
    ),
  )
}
