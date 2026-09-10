package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildOtherAccommodationReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildOtherAccommodationReferralSnapshot
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

class OtherAccommodationReferralMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot()

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name))
    assertThat(entity.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(entity.website).isEqualTo(snapshot.website)
    assertThat(entity.submissionNote).isEqualTo(snapshot.submissionNote)
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(status = OtherAccommodationReferralStatus.SUBMITTED)
    val createdBy = "Joe Bloggs"
    val createdByUsername = "joe.bloggs"
    val createdAt = Instant.now()
    val localAuthorityAreaName = "Test Local Authority"

    val dto = OtherAccommodationReferralMapper.toDto(
      snapshot = snapshot,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
      localAuthorityAreaName = localAuthorityAreaName,
    )

    assertThat(dto.caseId).isEqualTo(snapshot.caseId)
    assertThat(dto.crn).isEqualTo(snapshot.crn)
    assertThat(dto.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
    assertThat(dto.submission.id).isEqualTo(snapshot.id)
    assertThat(dto.submission.localAuthority.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(dto.submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaName)
    assertThat(dto.submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(dto.submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(dto.submission.createdBy).isEqualTo(createdBy)
    assertThat(dto.submission.createdByUsername).isEqualTo(createdByUsername)
    assertThat(dto.submission.createdAt).isEqualTo(createdAt)
    assertThat(dto.submission.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(dto.submission.website).isEqualTo(snapshot.website)
    assertThat(dto.submission.submissionNote).isEqualTo(snapshot.submissionNote)
  }

  @Test
  fun merge() {
    val entityId = UUID.randomUUID()
    val caseId = UUID.randomUUID()
    val entity = buildOtherAccommodationReferralEntity(
      id = entityId,
      caseId = caseId,
    )
    val preExistingNoteEntity = buildOtherAccommodationReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      otherAccommodationReferralEntity = entity,
    )
    entity.apply {
      notes.add(preExistingNoteEntity)
    }
    val newNote1 = buildOtherAccommodationReferralNote(id = UUID.randomUUID(), note = "2222")
    val newNote2 = buildOtherAccommodationReferralNote(id = UUID.randomUUID(), note = "3333")
    val preExistingNote = buildOtherAccommodationReferralNote(id = preExistingNoteEntity.id, note = preExistingNoteEntity.note)
    val snapshot = buildOtherAccommodationReferralSnapshot(
      notes = listOf(newNote1, newNote2, preExistingNote),
    )

    val merged = OtherAccommodationReferralMapper.merge(snapshot, entity)

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseId)
    assertThat(merged.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(merged.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(merged.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(merged.status).isEqualTo(EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name))
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val entity = buildOtherAccommodationReferralEntity(
      localAuthorityAreaId = UUID.randomUUID(),
      referenceNumber = "OA-REF-001",
      status = EntityOtherAccommodationReferralStatus.SUBMITTED,
    )
    val noteEntity = buildOtherAccommodationReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      otherAccommodationReferralEntity = entity,
    )
    val noteEntity2 = buildOtherAccommodationReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "2222",
      otherAccommodationReferralEntity = entity,
    )
    entity.apply {
      notes.add(noteEntity)
      notes.add(noteEntity2)
    }

    val aggregate = OtherAccommodationReferralMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(entity.id)
    assertThat(snapshot.caseId).isEqualTo(entity.caseId)
    assertThat(snapshot.crn).isEqualTo(entity.crn)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(entity.localAuthorityAreaId)
    assertThat(snapshot.referenceNumber).isEqualTo(entity.referenceNumber)
    assertThat(snapshot.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
    assertThat(snapshot.notes.first().id).isEqualTo(noteEntity.id)
    assertThat(snapshot.notes.first().note).isEqualTo(noteEntity.note)
    assertThat(snapshot.notes[1].id).isEqualTo(noteEntity2.id)
    assertThat(snapshot.notes[1].note).isEqualTo(noteEntity2.note)
  }
}
