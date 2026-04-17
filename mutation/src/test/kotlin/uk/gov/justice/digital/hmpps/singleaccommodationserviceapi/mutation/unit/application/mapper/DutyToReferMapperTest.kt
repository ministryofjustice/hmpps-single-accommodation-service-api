package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferSnapshot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class DutyToReferMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildDutyToReferSnapshot()

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityDtrStatus.valueOf(snapshot.status.name))
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `toEntity maps status enum values correctly`(
    status: DtrStatus,
  ) {
    val entity = DutyToReferMapper.toEntity(
      snapshot = buildDutyToReferSnapshot(status = status),
    )
    assertThat(entity.status).isEqualTo(EntityDtrStatus.valueOf(status.name))
  }

  @Test
  fun `toDto maps all fields correctly and sets status from snapshot`() {
    val snapshot = buildDutyToReferSnapshot(status = DtrStatus.ACCEPTED)
    val createdBy = "Joe Bloggs"
    val createdAt = Instant.now()
    val localAuthorityAreaName = "Test Local Authority"
    var crn = UUID.randomUUID().toString()

    val dto = DutyToReferMapper.toDto(snapshot, crn, createdBy, createdAt, localAuthorityAreaName)

    assertThat(dto.caseId).isEqualTo(snapshot.caseId)
    assertThat(dto.crn).isEqualTo(crn)
    assertThat(dto.status).isEqualTo(DtrStatus.ACCEPTED)
    assertThat(dto.submission).isNotNull()
    val submission = dto.submission!!
    assertThat(submission.id).isEqualTo(snapshot.id)
    assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaName)
    assertThat(submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(submission.createdBy).isEqualTo(createdBy)
    assertThat(submission.createdAt).isEqualTo(createdAt)
  }

  @Test
  fun merge() {
    val entityId = UUID.randomUUID()
    val caseId = UUID.randomUUID()
    val entity = buildDutyToReferEntity(
      id = entityId,
      caseId = caseId,
    )
    val preExistingNoteEntity = buildDutyToReferNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      dutyToReferEntity = entity,
    )
    entity.apply {
      notes.add(preExistingNoteEntity)
    }
    val newNote1 = buildDutyToReferNote(id = UUID.randomUUID(), note = "2222")
    val newNote2 = buildDutyToReferNote(id = UUID.randomUUID(), note = "3333")
    val preExistingNote = buildDutyToReferNote(id = preExistingNoteEntity.id, note = preExistingNoteEntity.note)
    val snapshot = buildDutyToReferSnapshot(
      notes = mutableListOf(newNote1, newNote2, preExistingNote),
    )
    val merged = DutyToReferMapper.merge(snapshot, entity)

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseId)
    assertThat(merged.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(merged.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(merged.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(merged.status).isEqualTo(EntityDtrStatus.valueOf(snapshot.status.name))
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val entity = buildDutyToReferEntity(
      localAuthorityAreaId = UUID.randomUUID(),
      referenceNumber = "DTR-REF-001",
      submissionDate = LocalDate.of(2026, 1, 15),
      status = EntityDtrStatus.SUBMITTED,
    )

    val noteEntity = buildDutyToReferNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      dutyToReferEntity = entity,
    )
    val noteEntity2 = buildDutyToReferNoteEntity(
      id = UUID.randomUUID(),
      note = "2222",
      dutyToReferEntity = entity,
    )
    entity.apply {
      notes.add(noteEntity)
      notes.add(noteEntity2)
    }

    val aggregate = DutyToReferMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(entity.id)
    assertThat(snapshot.caseId).isEqualTo(entity.caseId)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(entity.localAuthorityAreaId)
    assertThat(snapshot.referenceNumber).isEqualTo(entity.referenceNumber)
    assertThat(snapshot.submissionDate).isEqualTo(entity.submissionDate)
    assertThat(snapshot.status).isEqualTo(DtrStatus.SUBMITTED)
    assertThat(snapshot.notes.first().id).isEqualTo(noteEntity.id)
    assertThat(snapshot.notes.first().note).isEqualTo(noteEntity.note)
    assertThat(snapshot.notes[1].id).isEqualTo(noteEntity2.id)
    assertThat(snapshot.notes[1].note).isEqualTo(noteEntity2.note)
  }

  @ParameterizedTest
  @EnumSource(EntityDtrStatus::class)
  fun `toAggregate maps status enum values correctly`(
    status: EntityDtrStatus,
  ) {
    val entity = buildDutyToReferEntity(status = status)

    val aggregate = DutyToReferMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.status).isEqualTo(DtrStatus.valueOf(status.name))
  }
}
