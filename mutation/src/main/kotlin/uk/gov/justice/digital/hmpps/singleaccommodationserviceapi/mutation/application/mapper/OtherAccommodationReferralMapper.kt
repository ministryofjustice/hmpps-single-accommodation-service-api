package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate.OtherAccommodationReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason as EntityOutcomeReason

object OtherAccommodationReferralMapper {

  fun toEntity(snapshot: OtherAccommodationReferralSnapshot) = OtherAccommodationReferralEntity(
    id = snapshot.id,
    caseId = snapshot.caseId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityOorStatus.valueOf(snapshot.status.name),
    outcomeReason = snapshot.outcomeReason?.let { EntityOutcomeReason.valueOf(it.name) },
    submissionNote = snapshot.submissionNote,
    outcomeNote = snapshot.outcomeNote,
  )

  fun merge(snapshot: OtherAccommodationReferralSnapshot, entity: OtherAccommodationReferralEntity): OtherAccommodationReferralEntity {
    entity.referenceNumber = snapshot.referenceNumber
    entity.submissionDate = snapshot.submissionDate
    entity.status = EntityOorStatus.valueOf(snapshot.status.name)
    entity.outcomeReason = snapshot.outcomeReason?.let { EntityOutcomeReason.valueOf(it.name) }
    entity.submissionNote = snapshot.submissionNote
    entity.outcomeNote = snapshot.outcomeNote
    entity.addMissingNotes(snapshot.notes)
    return entity
  }

  fun OtherAccommodationReferralEntity.addMissingNotes(snapshotNotes: List<OtherAccommodationReferralAggregate.OtherAccommodationReferralNote>) {
    val existingIds = this.notes.map { it.id }.toSet()
    val missingNotes = snapshotNotes
      .filter { it.id !in existingIds }
      .map {
        OtherAccommodationReferralNoteEntity(
          id = it.id,
          note = it.note,
          otherAccommodationReferral = this,
        )
      }
    this.notes.addAll(missingNotes)
  }

  fun toAggregate(entity: OtherAccommodationReferralEntity): OtherAccommodationReferralAggregate = OtherAccommodationReferralAggregate.hydrateExisting(
    id = entity.id,
    caseId = entity.caseId,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    status = OorStatus.valueOf(entity.status.name),
    notes = entity.notes.map {
      OtherAccommodationReferralAggregate.OtherAccommodationReferralNote(
        id = it.id,
        note = it.note,
      )
    },
    outcomeReason = entity.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
    submissionNote = entity.submissionNote,
    outcomeNote = entity.outcomeNote,
  )

  fun toDto(
    snapshot: OtherAccommodationReferralSnapshot,
    crn: String,
    createdBy: String,
    createdAt: Instant,
  ) = OtherAccommodationReferralDto(
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
