package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate.ExternalReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralOutcomeReason as EntityExternalReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus as EntityExternalReferralStatus

object ExternalReferralMapper {

  fun toEntity(snapshot: ExternalReferralSnapshot) = ExternalReferralEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    caseId = snapshot.caseId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityExternalReferralStatus.valueOf(snapshot.status.name),
    organisationName = snapshot.organisationName,
    website = snapshot.website,
    submissionNote = snapshot.submissionNote,
    email = snapshot.email,
    phoneNumber = snapshot.phoneNumber,
    outcomeReason = snapshot.outcomeReason?.let { EntityExternalReferralOutcomeReason.valueOf(it.name) },
    outcomeNote = snapshot.outcomeNote,
  )

  fun merge(snapshot: ExternalReferralSnapshot, entity: ExternalReferralEntity): ExternalReferralEntity {
    entity.referenceNumber = snapshot.referenceNumber
    entity.submissionDate = snapshot.submissionDate
    entity.status = EntityExternalReferralStatus.valueOf(snapshot.status.name)
    entity.organisationName = snapshot.organisationName
    entity.website = snapshot.website
    entity.submissionNote = snapshot.submissionNote
    entity.email = snapshot.email
    entity.phoneNumber = snapshot.phoneNumber
    entity.outcomeReason = snapshot.outcomeReason?.let { EntityExternalReferralOutcomeReason.valueOf(it.name) }
    entity.outcomeNote = snapshot.outcomeNote
    entity.addMissingNotes(snapshot.notes)
    return entity
  }

  fun ExternalReferralEntity.addMissingNotes(snapshotNotes: List<ExternalReferralAggregate.ExternalReferralNote>) {
    val existingIds = notes.map { it.id }.toSet()
    notes.addAll(
      snapshotNotes
        .filter { it.id !in existingIds }
        .map { ExternalReferralNoteEntity(id = it.id, note = it.note, externalReferral = this) },
    )
  }

  fun toAggregate(entity: ExternalReferralEntity): ExternalReferralAggregate = ExternalReferralAggregate.hydrateExisting(
    id = entity.id,
    caseId = entity.caseId,
    crn = entity.crn,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    status = ExternalReferralStatus.valueOf(entity.status.name),
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    email = entity.email,
    phoneNumber = entity.phoneNumber,
    outcomeReason = entity.outcomeReason?.let { ExternalReferralOutcomeReason.valueOf(it.name) },
    outcomeNote = entity.outcomeNote,
    notes = entity.notes.map {
      ExternalReferralAggregate.ExternalReferralNote(
        id = it.id,
        note = it.note,
      )
    },
  )

  fun toDto(
    snapshot: ExternalReferralSnapshot,
    createdBy: String,
    createdByUsername: String,
    createdAt: Instant,
  ) = ExternalReferralDto(
    caseId = snapshot.caseId,
    crn = snapshot.crn,
    status = ExternalReferralStatus.valueOf(snapshot.status.name),
    submission = ExternalReferralSubmissionDto(
      id = snapshot.id,
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
      organisationName = snapshot.organisationName,
      website = snapshot.website,
      submissionNote = snapshot.submissionNote,
      email = snapshot.email,
      phoneNumber = snapshot.phoneNumber,
      outcomeReason = snapshot.outcomeReason,
      outcomeNote = snapshot.outcomeNote,
    ),
  )
}
