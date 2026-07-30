package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferSnapshot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason as EntityOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.WithdrawalReason as EntityWithdrawalReason

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
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED", "WITHDRAWN"])
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

  @Test
  fun `toEntity maps withdrawal fields correctly`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.NEW_REFERRAL,
      withdrawalReasonOther = null,
    )

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.withdrawalReason).isEqualTo(EntityWithdrawalReason.NEW_REFERRAL)
    assertThat(entity.withdrawalReasonOther).isNull()
  }

  @Test
  fun `toEntity maps OTHER withdrawal reason with text correctly`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.OTHER,
      withdrawalReasonOther = "custom reason",
    )

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.withdrawalReason).isEqualTo(EntityWithdrawalReason.OTHER)
    assertThat(entity.withdrawalReasonOther).isEqualTo("custom reason")
  }

  @Test
  fun `toAggregate maps withdrawal fields correctly`() {
    val entity = buildDutyToReferEntity(
      status = EntityDtrStatus.WITHDRAWN,
      withdrawalReason = EntityWithdrawalReason.DISENGAGED,
      withdrawalReasonOther = null,
    )

    val aggregate = DutyToReferMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.withdrawalReason).isEqualTo(WithdrawalReason.DISENGAGED)
    assertThat(snapshot.withdrawalReasonOther).isNull()
  }

  @Test
  fun `toDto maps withdrawal fields onto submission when status is WITHDRAWN`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.NO_CONSENT,
      withdrawalReasonOther = null,
    )

    val dto = DutyToReferMapper.toDto(snapshot, "X123456", "Test User", Instant.now(), "Test LA")

    assertThat(dto.submission!!.withdrawalReason).isEqualTo(WithdrawalReason.NO_CONSENT)
    assertThat(dto.submission!!.withdrawalReasonOther).isNull()
  }

  @Test
  fun `toDto sets null withdrawal fields on submission when status is not WITHDRAWN`() {
    val snapshot = buildDutyToReferSnapshot(status = DtrStatus.ACCEPTED)

    val dto = DutyToReferMapper.toDto(snapshot, "X123456", "Test User", Instant.now(), "Test LA")

    assertThat(dto.submission!!.withdrawalReason).isNull()
    assertThat(dto.submission!!.withdrawalReasonOther).isNull()
  }

  @Test
  fun `merge copies withdrawal fields from snapshot to entity`() {
    val entity = buildDutyToReferEntity(
      status = EntityDtrStatus.SUBMITTED,
      withdrawalReason = null,
      withdrawalReasonOther = null,
    )
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.HOUSING_NEED_RESOLVED,
      withdrawalReasonOther = null,
    )

    val merged = DutyToReferMapper.merge(snapshot, entity)

    assertThat(merged.withdrawalReason).isEqualTo(EntityWithdrawalReason.HOUSING_NEED_RESOLVED)
    assertThat(merged.withdrawalReasonOther).isNull()
  }

  @Test
  fun `toEntity maps outcome reason correctly`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
    )

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.outcomeReason).isEqualTo(EntityOutcomeReason.PRIORITY_NEED)
  }

  @Test
  fun `toAggregate maps outcome reason correctly`() {
    val entity = buildDutyToReferEntity(
      status = EntityDtrStatus.NOT_ACCEPTED,
      outcomeReason = EntityOutcomeReason.NO_LOCAL_CONNECTION,
    )

    val aggregate = DutyToReferMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.outcomeReason).isEqualTo(OutcomeReason.NO_LOCAL_CONNECTION)
  }

  @Test
  fun `toDto maps outcome reason onto submission`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PREVENTION_AND_RELIEF_DUTY,
    )

    val dto = DutyToReferMapper.toDto(snapshot, "X123456", "Test User", Instant.now(), "Test LA")

    assertThat(dto.submission!!.outcomeReason).isEqualTo(OutcomeReason.PREVENTION_AND_RELIEF_DUTY)
  }

  @Test
  fun `merge copies outcome reason from snapshot to entity`() {
    val entity = buildDutyToReferEntity(
      status = EntityDtrStatus.SUBMITTED,
      outcomeReason = null,
    )
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.NOT_ACCEPTED,
      outcomeReason = OutcomeReason.INTENTIONALLY_HOMELESS,
    )

    val merged = DutyToReferMapper.merge(snapshot, entity)

    assertThat(merged.outcomeReason).isEqualTo(EntityOutcomeReason.INTENTIONALLY_HOMELESS)
  }

  @Test
  fun `toEntity maps submissionNote and outcomeNote correctly`() {
    val snapshot = buildDutyToReferSnapshot(
      submissionNote = "My submission note",
      outcomeNote = "My outcome note",
    )

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.submissionNote).isEqualTo("My submission note")
    assertThat(entity.outcomeNote).isEqualTo("My outcome note")
  }

  @Test
  fun `toEntity maps null submissionNote and outcomeNote correctly`() {
    val snapshot = buildDutyToReferSnapshot(submissionNote = null, outcomeNote = null)

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.submissionNote).isNull()
    assertThat(entity.outcomeNote).isNull()
  }

  @Test
  fun `toAggregate maps submissionNote and outcomeNote correctly`() {
    val entity = buildDutyToReferEntity(
      submissionNote = "Submission note",
      outcomeNote = "Outcome note",
    )

    val aggregate = DutyToReferMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.submissionNote).isEqualTo("Submission note")
    assertThat(snapshot.outcomeNote).isEqualTo("Outcome note")
  }

  @Test
  fun `toDto maps submissionNote and outcomeNote onto submission`() {
    val snapshot = buildDutyToReferSnapshot(
      status = DtrStatus.ACCEPTED,
      submissionNote = "A submission note",
      outcomeNote = "An outcome note",
    )

    val dto = DutyToReferMapper.toDto(snapshot, "X123456", "Test User", Instant.now(), "Test LA")

    assertThat(dto.submission!!.submissionNote).isEqualTo("A submission note")
    assertThat(dto.submission!!.outcomeNote).isEqualTo("An outcome note")
  }

  @Test
  fun `merge copies submissionNote and outcomeNote from snapshot to entity`() {
    val entity = buildDutyToReferEntity(submissionNote = null, outcomeNote = null)
    val snapshot = buildDutyToReferSnapshot(
      submissionNote = "Submission note",
      outcomeNote = "Outcome note",
    )

    val merged = DutyToReferMapper.merge(snapshot, entity)

    assertThat(merged.submissionNote).isEqualTo("Submission note")
    assertThat(merged.outcomeNote).isEqualTo("Outcome note")
  }

  @Test
  fun `merge preserves existing submissionNote when outcomeNote is set`() {
    val entity = buildDutyToReferEntity(submissionNote = "Original submission note", outcomeNote = null)
    val snapshot = buildDutyToReferSnapshot(
      submissionNote = "Original submission note",
      outcomeNote = "New outcome note",
    )

    val merged = DutyToReferMapper.merge(snapshot, entity)

    assertThat(merged.submissionNote).isEqualTo("Original submission note")
    assertThat(merged.outcomeNote).isEqualTo("New outcome note")
  }
}
