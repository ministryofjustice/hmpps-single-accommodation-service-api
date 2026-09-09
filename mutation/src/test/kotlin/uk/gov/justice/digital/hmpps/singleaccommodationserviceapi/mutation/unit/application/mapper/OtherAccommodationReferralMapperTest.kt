package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildOtherAccommodationReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildOtherAccommodationReferralSnapshot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason as EntityOutcomeReason

class OtherAccommodationReferralMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot()

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityOorStatus.valueOf(snapshot.status.name))
  }

  @ParameterizedTest
  @EnumSource(OorStatus::class)
  fun `toEntity maps status enum values correctly`(
    status: OorStatus,
  ) {
    val entity = OtherAccommodationReferralMapper.toEntity(
      snapshot = buildOtherAccommodationReferralSnapshot(status = status),
    )
    assertThat(entity.status).isEqualTo(EntityOorStatus.valueOf(status.name))
  }

  @Test
  fun `toDto maps all fields correctly and sets status from snapshot`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(status = OorStatus.ACCEPTED)
    val createdBy = "Joe Bloggs"
    val createdAt = Instant.now()
    var crn = UUID.randomUUID().toString()

    val dto = OtherAccommodationReferralMapper.toDto(snapshot, crn, createdBy, createdAt)

    assertThat(dto.caseId).isEqualTo(snapshot.caseId)
    assertThat(dto.crn).isEqualTo(crn)
    assertThat(dto.status).isEqualTo(OorStatus.ACCEPTED)
    assertThat(dto.submission).isNotNull()
    val submission = dto.submission!!
    assertThat(submission.id).isEqualTo(snapshot.id)
    assertThat(submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(submission.createdBy).isEqualTo(createdBy)
    assertThat(submission.createdAt).isEqualTo(createdAt)
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
      notes = mutableListOf(newNote1, newNote2, preExistingNote),
    )
    val merged = OtherAccommodationReferralMapper.merge(snapshot, entity)

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseId)
    assertThat(merged.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(merged.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(merged.status).isEqualTo(EntityOorStatus.valueOf(snapshot.status.name))
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val entity = buildOtherAccommodationReferralEntity(
      referenceNumber = "OOR-REF-001",
      submissionDate = LocalDate.of(2026, 1, 15),
      status = EntityOorStatus.SUBMITTED,
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
    assertThat(snapshot.referenceNumber).isEqualTo(entity.referenceNumber)
    assertThat(snapshot.submissionDate).isEqualTo(entity.submissionDate)
    assertThat(snapshot.status).isEqualTo(OorStatus.SUBMITTED)
    assertThat(snapshot.notes.first().id).isEqualTo(noteEntity.id)
    assertThat(snapshot.notes.first().note).isEqualTo(noteEntity.note)
    assertThat(snapshot.notes[1].id).isEqualTo(noteEntity2.id)
    assertThat(snapshot.notes[1].note).isEqualTo(noteEntity2.note)
  }

  @ParameterizedTest
  @EnumSource(EntityOorStatus::class)
  fun `toAggregate maps status enum values correctly`(
    status: EntityOorStatus,
  ) {
    val entity = buildOtherAccommodationReferralEntity(status = status)

    val aggregate = OtherAccommodationReferralMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.status).isEqualTo(OorStatus.valueOf(status.name))
  }

  @Test
  fun `toEntity maps outcome reason correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
    )

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.outcomeReason).isEqualTo(EntityOutcomeReason.PRIORITY_NEED)
  }

  @Test
  fun `toAggregate maps outcome reason correctly`() {
    val entity = buildOtherAccommodationReferralEntity(
      status = EntityOorStatus.NOT_ACCEPTED,
      outcomeReason = EntityOutcomeReason.NO_LOCAL_CONNECTION,
    )

    val aggregate = OtherAccommodationReferralMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.outcomeReason).isEqualTo(OutcomeReason.NO_LOCAL_CONNECTION)
  }

  @Test
  fun `toDto maps outcome reason onto submission`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PREVENTION_AND_RELIEF_DUTY,
    )

    val dto = OtherAccommodationReferralMapper.toDto(snapshot, "X123456", "Test User", Instant.now())

    assertThat(dto.submission!!.outcomeReason).isEqualTo(OutcomeReason.PREVENTION_AND_RELIEF_DUTY)
  }

  @Test
  fun `merge copies outcome reason from snapshot to entity`() {
    val entity = buildOtherAccommodationReferralEntity(
      status = EntityOorStatus.SUBMITTED,
      outcomeReason = null,
    )
    val snapshot = buildOtherAccommodationReferralSnapshot(
      status = OorStatus.NOT_ACCEPTED,
      outcomeReason = OutcomeReason.INTENTIONALLY_HOMELESS,
    )

    val merged = OtherAccommodationReferralMapper.merge(snapshot, entity)

    assertThat(merged.outcomeReason).isEqualTo(EntityOutcomeReason.INTENTIONALLY_HOMELESS)
  }

  @Test
  fun `toEntity maps submissionNote and outcomeNote correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(
      submissionNote = "My submission note",
      outcomeNote = "My outcome note",
    )

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.submissionNote).isEqualTo("My submission note")
    assertThat(entity.outcomeNote).isEqualTo("My outcome note")
  }

  @Test
  fun `toEntity maps null submissionNote and outcomeNote correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(submissionNote = null, outcomeNote = null)

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.submissionNote).isNull()
    assertThat(entity.outcomeNote).isNull()
  }

  @Test
  fun `toAggregate maps submissionNote and outcomeNote correctly`() {
    val entity = buildOtherAccommodationReferralEntity(
      submissionNote = "Submission note",
      outcomeNote = "Outcome note",
    )

    val aggregate = OtherAccommodationReferralMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.submissionNote).isEqualTo("Submission note")
    assertThat(snapshot.outcomeNote).isEqualTo("Outcome note")
  }

  @Test
  fun `toDto maps submissionNote and outcomeNote onto submission`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(
      status = OorStatus.ACCEPTED,
      submissionNote = "A submission note",
      outcomeNote = "An outcome note",
    )

    val dto = OtherAccommodationReferralMapper.toDto(snapshot, "X123456", "Test User", Instant.now())

    assertThat(dto.submission!!.submissionNote).isEqualTo("A submission note")
    assertThat(dto.submission!!.outcomeNote).isEqualTo("An outcome note")
  }

  @Test
  fun `merge copies submissionNote and outcomeNote from snapshot to entity`() {
    val entity = buildOtherAccommodationReferralEntity(submissionNote = null, outcomeNote = null)
    val snapshot = buildOtherAccommodationReferralSnapshot(
      submissionNote = "Submission note",
      outcomeNote = "Outcome note",
    )

    val merged = OtherAccommodationReferralMapper.merge(snapshot, entity)

    assertThat(merged.submissionNote).isEqualTo("Submission note")
    assertThat(merged.outcomeNote).isEqualTo("Outcome note")
  }

  @Test
  fun `merge preserves existing submissionNote when outcomeNote is set`() {
    val entity = buildOtherAccommodationReferralEntity(submissionNote = "Original submission note", outcomeNote = null)
    val snapshot = buildOtherAccommodationReferralSnapshot(
      submissionNote = "Original submission note",
      outcomeNote = "New outcome note",
    )

    val merged = OtherAccommodationReferralMapper.merge(snapshot, entity)

    assertThat(merged.submissionNote).isEqualTo("Original submission note")
    assertThat(merged.outcomeNote).isEqualTo("New outcome note")
  }
}
