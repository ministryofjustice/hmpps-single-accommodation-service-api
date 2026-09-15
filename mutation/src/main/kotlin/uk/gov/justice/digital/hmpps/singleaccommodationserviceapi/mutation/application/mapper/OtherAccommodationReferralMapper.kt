package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate.OtherAccommodationReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

object OtherAccommodationReferralMapper {

  fun toEntity(snapshot: OtherAccommodationReferralSnapshot) = OtherAccommodationReferralEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    caseId = snapshot.caseId,
    localAuthorityAreaId = snapshot.localAuthorityAreaId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name),
    organisationName = snapshot.organisationName,
    website = snapshot.website,
    submissionNote = snapshot.submissionNote,
  )

  fun merge(snapshot: OtherAccommodationReferralSnapshot, entity: OtherAccommodationReferralEntity): OtherAccommodationReferralEntity {
    entity.localAuthorityAreaId = snapshot.localAuthorityAreaId
    entity.referenceNumber = snapshot.referenceNumber
    entity.submissionDate = snapshot.submissionDate
    entity.status = EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name)
    entity.organisationName = snapshot.organisationName
    entity.website = snapshot.website
    entity.submissionNote = snapshot.submissionNote
    entity.addMissingNotes(snapshot.notes)
    return entity
  }

  fun OtherAccommodationReferralEntity.addMissingNotes(snapshotNotes: List<OtherAccommodationReferralAggregate.OtherAccommodationReferralNote>) {
    val existingIds = notes.map { it.id }.toSet()
    notes.addAll(
      snapshotNotes
        .filter { it.id !in existingIds }
        .map { OtherAccommodationReferralNoteEntity(id = it.id, note = it.note, otherAccommodationReferral = this) },
    )
  }

  fun toAggregate(entity: OtherAccommodationReferralEntity): OtherAccommodationReferralAggregate = OtherAccommodationReferralAggregate.hydrateExisting(
    id = entity.id,
    caseId = entity.caseId,
    crn = entity.crn,
    localAuthorityAreaId = entity.localAuthorityAreaId,
    referenceNumber = entity.referenceNumber,
    submissionDate = entity.submissionDate,
    status = OtherAccommodationReferralStatus.valueOf(entity.status.name),
    organisationName = entity.organisationName,
    website = entity.website,
    submissionNote = entity.submissionNote,
    notes = entity.notes.map {
      OtherAccommodationReferralAggregate.OtherAccommodationReferralNote(
        id = it.id,
        note = it.note,
      )
    },
  )

  fun toDto(
    snapshot: OtherAccommodationReferralSnapshot,
    createdBy: String,
    createdByUsername: String?,
    createdAt: Instant,
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralDto(
    caseId = snapshot.caseId,
    crn = snapshot.crn,
    status = OtherAccommodationReferralStatus.valueOf(snapshot.status.name),
    submission = OtherAccommodationReferralSubmissionDto(
      id = snapshot.id,
      localAuthority = LocalAuthorityDto(
        localAuthorityAreaId = snapshot.localAuthorityAreaId,
        localAuthorityAreaName = localAuthorityAreaName,
      ),
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
      organisationName = snapshot.organisationName,
      website = snapshot.website,
      submissionNote = snapshot.submissionNote,
    ),
  )
}
