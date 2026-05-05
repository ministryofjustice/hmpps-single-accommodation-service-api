package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildProposedAccommodationSnapshot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val accommodationTypeEntity = buildAccommodationTypeEntity()
    val entity = ProposedAccommodationMapper.toEntity(snapshot, accommodationTypeEntity)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.name).isEqualTo(snapshot.name)
    assertThat(entity.accommodationTypeId).isEqualTo(accommodationTypeEntity.id)
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(snapshot.verificationStatus.name))
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    assertThat(entity.startDate).isEqualTo(snapshot.startDate)
    assertThat(entity.endDate).isEqualTo(snapshot.endDate)
    assertThat(entity.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(entity.subBuildingName).isEqualTo(snapshot.address.subBuildingName)
    assertThat(entity.buildingName).isEqualTo(snapshot.address.buildingName)
    assertThat(entity.buildingNumber).isEqualTo(snapshot.address.buildingNumber)
    assertThat(entity.throughfareName).isEqualTo(snapshot.address.thoroughfareName)
    assertThat(entity.dependentLocality).isEqualTo(snapshot.address.dependentLocality)
    assertThat(entity.postTown).isEqualTo(snapshot.address.postTown)
    assertThat(entity.county).isEqualTo(snapshot.address.county)
    assertThat(entity.country).isEqualTo(snapshot.address.country)
    assertThat(entity.uprn).isEqualTo(snapshot.address.uprn)
  }

  @ParameterizedTest
  @EnumSource(VerificationStatus::class)
  fun `toEntity maps verificationStatus enum values correctly`(
    verificationStatus: VerificationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(verificationStatus = verificationStatus),
      accommodationTypeEntity = buildAccommodationTypeEntity(),
    )
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(verificationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(NextAccommodationStatus::class)
  fun `toEntity maps nextAccommodationStatus enum values correctly`(
    nextAccommodationStatus: NextAccommodationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(nextAccommodationStatus = nextAccommodationStatus),
      accommodationTypeEntity = buildAccommodationTypeEntity(),
    )
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(nextAccommodationStatus.name))
  }

  @Test
  fun `applyToEntity should copy all fields from snapshot to entity`() {
    val entityId = UUID.randomUUID()
    val caseID = UUID.randomUUID()
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      id = entityId,
      caseId = caseID,
    )
    val accommodationTypeEntity = buildAccommodationTypeEntity()
    val preExistingNoteEntity = buildProposedAccommodationNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      proposedAccommodationEntity = proposedAccommodationEntity,
    )
    proposedAccommodationEntity.apply {
      notes.add(preExistingNoteEntity)
    }
    val newNote1 = buildNote(id = UUID.randomUUID(), note = "2222")
    val newNote2 = buildNote(id = UUID.randomUUID(), note = "3333")
    val preExistingNote = buildNote(id = preExistingNoteEntity.id, note = preExistingNoteEntity.note)
    val snapshot = buildProposedAccommodationSnapshot(
      notes = mutableListOf(newNote1, newNote2, preExistingNote),
    )
    val merged = ProposedAccommodationMapper.merge(snapshot, proposedAccommodationEntity, accommodationTypeEntity)

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseID)
    assertThat(merged.name).isEqualTo(snapshot.name)
    assertThat(merged.accommodationTypeId).isEqualTo(accommodationTypeEntity.id)
    assertThat(merged.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(snapshot.verificationStatus.name))
    assertThat(merged.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    assertThat(merged.startDate).isEqualTo(snapshot.startDate)
    assertThat(merged.endDate).isEqualTo(snapshot.endDate)
    assertThat(merged.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(merged.subBuildingName).isEqualTo(snapshot.address.subBuildingName)
    assertThat(merged.buildingName).isEqualTo(snapshot.address.buildingName)
    assertThat(merged.buildingNumber).isEqualTo(snapshot.address.buildingNumber)
    assertThat(merged.throughfareName).isEqualTo(snapshot.address.thoroughfareName)
    assertThat(merged.dependentLocality).isEqualTo(snapshot.address.dependentLocality)
    assertThat(merged.postTown).isEqualTo(snapshot.address.postTown)
    assertThat(merged.county).isEqualTo(snapshot.address.county)
    assertThat(merged.country).isEqualTo(snapshot.address.country)
    assertThat(merged.uprn).isEqualTo(snapshot.address.uprn)
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val accommodationTypeEntity = buildAccommodationTypeEntity()
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      name = "Test Name",
      accommodationTypeEntity = accommodationTypeEntity,
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      postcode = "SW1A 1AA",
      subBuildingName = "Sub",
      buildingName = "Building",
      buildingNumber = "10",
      throughfareName = "Downing Street",
      dependentLocality = "Westminster",
      postTown = "London",
      county = "London",
      country = "England",
      uprn = "12345",
      startDate = LocalDate.of(2026, 1, 5),
      endDate = LocalDate.of(2026, 4, 25),
    )

    val noteEntity = buildProposedAccommodationNoteEntity(
      id = UUID.randomUUID(),
      note = "1111",
      proposedAccommodationEntity = proposedAccommodationEntity,
    )
    val noteEntity2 = buildProposedAccommodationNoteEntity(
      id = UUID.randomUUID(),
      note = "2222",
      proposedAccommodationEntity = proposedAccommodationEntity,
    )
    proposedAccommodationEntity.apply {
      notes.add(noteEntity)
      notes.add(noteEntity2)
    }

    val aggregate = ProposedAccommodationMapper.toAggregate(proposedAccommodationEntity, accommodationTypeEntity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(proposedAccommodationEntity.id)
    assertThat(snapshot.caseId).isEqualTo(proposedAccommodationEntity.caseId)
    assertThat(snapshot.name).isEqualTo(proposedAccommodationEntity.name)
    assertThat(snapshot.accommodationType.code).isEqualTo(accommodationTypeEntity.code)
    assertThat(snapshot.accommodationType.description).isEqualTo(accommodationTypeEntity.name)
    assertThat(snapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(snapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(snapshot.address.postcode).isEqualTo(proposedAccommodationEntity.postcode)
    assertThat(snapshot.address.subBuildingName).isEqualTo(proposedAccommodationEntity.subBuildingName)
    assertThat(snapshot.address.buildingName).isEqualTo(proposedAccommodationEntity.buildingName)
    assertThat(snapshot.address.buildingNumber).isEqualTo(proposedAccommodationEntity.buildingNumber)
    assertThat(snapshot.address.thoroughfareName).isEqualTo(proposedAccommodationEntity.throughfareName)
    assertThat(snapshot.address.dependentLocality).isEqualTo(proposedAccommodationEntity.dependentLocality)
    assertThat(snapshot.address.postTown).isEqualTo(proposedAccommodationEntity.postTown)
    assertThat(snapshot.address.county).isEqualTo(proposedAccommodationEntity.county)
    assertThat(snapshot.address.country).isEqualTo(proposedAccommodationEntity.country)
    assertThat(snapshot.address.uprn).isEqualTo(proposedAccommodationEntity.uprn)
    assertThat(snapshot.startDate).isEqualTo(proposedAccommodationEntity.startDate)
    assertThat(snapshot.endDate).isEqualTo(proposedAccommodationEntity.endDate)
    assertThat(snapshot.notes.first().id).isEqualTo(noteEntity.id)
    assertThat(snapshot.notes.first().note).isEqualTo(noteEntity.note)
    assertThat(snapshot.notes[1].id).isEqualTo(noteEntity2.id)
    assertThat(snapshot.notes[1].note).isEqualTo(noteEntity2.note)
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val crn = UUID.randomUUID().toString()
    val createdBy = "Joe Bloggs"
    val createdAt = Instant.now()
    val dto = ProposedAccommodationMapper.toDto(snapshot, crn, createdBy, createdAt)
    assertThat(dto.id).isEqualTo(snapshot.id)
    assertThat(dto.crn).isEqualTo(crn)
    assertThat(dto.name).isEqualTo(snapshot.name)
    assertThat(dto.accommodationType).isEqualTo(snapshot.accommodationType)
    assertThat(dto.verificationStatus).isEqualTo(snapshot.verificationStatus)
    assertThat(dto.nextAccommodationStatus).isEqualTo(snapshot.nextAccommodationStatus)
    assertThat(dto.startDate).isEqualTo(snapshot.startDate)
    assertThat(dto.endDate).isEqualTo(snapshot.endDate)
    assertThat(dto.address).isEqualTo(snapshot.address)
    assertThat(dto.createdBy).isEqualTo(createdBy)
    assertThat(dto.createdAt).isEqualTo(createdAt)
  }
}
