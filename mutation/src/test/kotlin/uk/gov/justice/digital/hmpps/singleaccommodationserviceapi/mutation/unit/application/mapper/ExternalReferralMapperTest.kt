package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ExternalReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildExternalReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildExternalReferralSnapshot
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus as EntityExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason as EntityExternalReferralWithdrawalReason

class ExternalReferralMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildExternalReferralSnapshot(
      status = ExternalReferralStatus.ACCEPTED,
      withdrawalReason = ExternalReferralWithdrawalReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT,
      outcomeNote = "An outcome note",
      email = "contact@example.com",
      phoneNumber = "01234567890",
    )

    val entity = ExternalReferralMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityExternalReferralStatus.valueOf(snapshot.status.name))
    assertThat(entity.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(entity.website).isEqualTo(snapshot.website)
    assertThat(entity.submissionNote).isEqualTo(snapshot.submissionNote)
    assertThat(entity.email).isEqualTo(snapshot.email)
    assertThat(entity.phoneNumber).isEqualTo(snapshot.phoneNumber)
    assertThat(entity.withdrawalReason).isEqualTo(EntityExternalReferralWithdrawalReason.valueOf(snapshot.withdrawalReason!!.name))
    assertThat(entity.outcomeNote).isEqualTo(snapshot.outcomeNote)
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildExternalReferralSnapshot(
      status = ExternalReferralStatus.REJECTED,
      withdrawalReason = ExternalReferralWithdrawalReason.NO_CAPACITY,
      outcomeNote = "An outcome note",
      email = "contact@example.com",
      phoneNumber = "01234567890",
    )
    val createdBy = "Joe Bloggs"
    val createdByUsername = "joe.bloggs"
    val createdAt = Instant.now()

    val dto = ExternalReferralMapper.toDto(
      snapshot = snapshot,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
    )

    assertThat(dto.caseId).isEqualTo(snapshot.caseId)
    assertThat(dto.crn).isEqualTo(snapshot.crn)
    assertThat(dto.status).isEqualTo(ExternalReferralStatus.REJECTED)
    assertThat(dto.submission.id).isEqualTo(snapshot.id)
    assertThat(dto.submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(dto.submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(dto.submission.createdBy).isEqualTo(createdBy)
    assertThat(dto.submission.createdByUsername).isEqualTo(createdByUsername)
    assertThat(dto.submission.createdAt).isEqualTo(createdAt)
    assertThat(dto.submission.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(dto.submission.website).isEqualTo(snapshot.website)
    assertThat(dto.submission.submissionNote).isEqualTo(snapshot.submissionNote)
    assertThat(dto.submission.email).isEqualTo(snapshot.email)
    assertThat(dto.submission.phoneNumber).isEqualTo(snapshot.phoneNumber)
    assertThat(dto.submission.outcomeNote).isEqualTo(snapshot.outcomeNote)
  }

  @Test
  fun merge() {
    val entityId = UUID.randomUUID()
    val caseId = UUID.randomUUID()
    val entity = buildExternalReferralEntity(
      id = entityId,
      caseId = caseId,
    )
    val preExistingNoteEntity = buildExternalReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      externalReferralEntity = entity,
    )
    entity.apply {
      notes.add(preExistingNoteEntity)
    }
    val newNote1 = buildExternalReferralNote(id = UUID.randomUUID(), note = "2222")
    val newNote2 = buildExternalReferralNote(id = UUID.randomUUID(), note = "3333")
    val preExistingNote = buildExternalReferralNote(id = preExistingNoteEntity.id, note = preExistingNoteEntity.note)
    val snapshot = buildExternalReferralSnapshot(
      status = ExternalReferralStatus.ACCEPTED,
      withdrawalReason = ExternalReferralWithdrawalReason.ACCEPTED_BY_ORGANISATION,
      outcomeNote = "An outcome note",
      email = "contact@example.com",
      phoneNumber = "01234567890",
      notes = listOf(newNote1, newNote2, preExistingNote),
    )

    val merged = ExternalReferralMapper.merge(snapshot, entity)

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseId)
    assertThat(merged.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(merged.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(merged.status).isEqualTo(EntityExternalReferralStatus.valueOf(snapshot.status.name))
    assertThat(merged.email).isEqualTo(snapshot.email)
    assertThat(merged.phoneNumber).isEqualTo(snapshot.phoneNumber)
    assertThat(merged.withdrawalReason).isEqualTo(EntityExternalReferralWithdrawalReason.ACCEPTED_BY_ORGANISATION)
    assertThat(merged.outcomeNote).isEqualTo(snapshot.outcomeNote)
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val entity = buildExternalReferralEntity(
      referenceNumber = "OA-REF-001",
      status = EntityExternalReferralStatus.REJECTED,
      withdrawalReason = EntityExternalReferralWithdrawalReason.PERSON_NOT_SUITABLE,
      outcomeNote = "An outcome note",
      email = "contact@example.com",
      phoneNumber = "01234567890",
    )
    val noteEntity = buildExternalReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      externalReferralEntity = entity,
    )
    val noteEntity2 = buildExternalReferralNoteEntity(
      id = UUID.randomUUID(),
      note = "2222",
      externalReferralEntity = entity,
    )
    entity.apply {
      notes.add(noteEntity)
      notes.add(noteEntity2)
    }

    val aggregate = ExternalReferralMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(entity.id)
    assertThat(snapshot.caseId).isEqualTo(entity.caseId)
    assertThat(snapshot.crn).isEqualTo(entity.crn)
    assertThat(snapshot.referenceNumber).isEqualTo(entity.referenceNumber)
    assertThat(snapshot.status).isEqualTo(ExternalReferralStatus.REJECTED)
    assertThat(snapshot.withdrawalReason).isEqualTo(ExternalReferralWithdrawalReason.PERSON_NOT_SUITABLE)
    assertThat(snapshot.outcomeNote).isEqualTo(entity.outcomeNote)
    assertThat(snapshot.email).isEqualTo(entity.email)
    assertThat(snapshot.phoneNumber).isEqualTo(entity.phoneNumber)
    assertThat(snapshot.notes.first().id).isEqualTo(noteEntity.id)
    assertThat(snapshot.notes.first().note).isEqualTo(noteEntity.note)
    assertThat(snapshot.notes[1].id).isEqualTo(noteEntity2.id)
    assertThat(snapshot.notes[1].note).isEqualTo(noteEntity2.note)
  }
}
