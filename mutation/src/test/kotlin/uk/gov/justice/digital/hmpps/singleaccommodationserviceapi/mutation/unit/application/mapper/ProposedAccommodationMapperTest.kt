package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
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
    val snapshot = buildProposedAccommodationSnapshot(
      cprAddressId = UUID.randomUUID(),
    )
    val accommodationTypeEntity = buildAccommodationTypeEntity()
    val accommodationStatusEntity = buildAccommodationStatusEntity()
    val entity = ProposedAccommodationMapper.toEntity(snapshot, accommodationTypeEntity, accommodationStatusEntity)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.cprAddressId).isEqualTo(snapshot.cprAddressId)
    assertThat(entity.accommodationTypeId).isEqualTo(accommodationTypeEntity.id)
    assertThat(entity.accommodationStatusId).isEqualTo(accommodationStatusEntity.id)
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(snapshot.verificationStatus.name))
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    assertThat(entity.startDate).isEqualTo(snapshot.startDate)
    assertThat(entity.endDate).isEqualTo(snapshot.endDate)
    assertThat(entity.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(entity.subBuildingName).isEqualTo(snapshot.address.subBuildingName)
    assertThat(entity.buildingName).isEqualTo(snapshot.address.buildingName)
    assertThat(entity.buildingNumber).isEqualTo(snapshot.address.buildingNumber)
    assertThat(entity.thoroughfareName).isEqualTo(snapshot.address.thoroughfareName)
    assertThat(entity.dependentLocality).isEqualTo(snapshot.address.dependentLocality)
    assertThat(entity.postTown).isEqualTo(snapshot.address.postTown)
    assertThat(entity.county).isEqualTo(snapshot.address.county)
    assertThat(entity.country).isNull()
    assertThat(entity.uprn).isEqualTo(snapshot.address.uprn)
    assertThat(entity.accommodationSource).isEqualTo(snapshot.accommodationSource)
    assertThat(entity.typeVerified).isEqualTo(snapshot.typeVerified)
    assertThat(entity.noFixedAbode).isEqualTo(snapshot.noFixedAbode)
  }

  @Test
  fun `toEntity maps all empty address fields to null`() {
    val snapshot = buildProposedAccommodationSnapshot(
      cprAddressId = UUID.randomUUID(),
      address = buildAccommodationAddressDetails(
        postcode = "SW1A 1AA",
        subBuildingName = "",
        buildingName = "",
        buildingNumber = "",
        thoroughfareName = "",
        dependentLocality = "",
        postTown = "",
        county = "",
        country = "",
        uprn = "",
      ),
    )
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )
    assertThat(entity.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(entity.subBuildingName).isNull()
    assertThat(entity.buildingName).isNull()
    assertThat(entity.buildingNumber).isNull()
    assertThat(entity.thoroughfareName).isNull()
    assertThat(entity.dependentLocality).isNull()
    assertThat(entity.postTown).isNull()
    assertThat(entity.county).isNull()
    assertThat(entity.country).isNull()
    assertThat(entity.uprn).isNull()
  }

  @Test
  fun `toEntity maps accommodation source type verified and no fixed abode`() {
    val snapshot = buildProposedAccommodationSnapshot(
      accommodationSource = AccommodationSource.DELIUS,
      typeVerified = true,
      noFixedAbode = true,
    )

    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = snapshot,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )

    assertThat(entity.accommodationSource).isEqualTo(snapshot.accommodationSource)
    assertThat(entity.typeVerified).isEqualTo(snapshot.typeVerified)
    assertThat(entity.noFixedAbode).isEqualTo(snapshot.noFixedAbode)
  }

  @Test
  fun `toEntity maps null accommodation status entity`() {
    val snapshot = buildProposedAccommodationSnapshot()

    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = snapshot,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = null,
    )

    assertThat(entity.accommodationStatusId).isNull()
  }

  @Test
  fun `toEntity maps null accommodation type entity`() {
    val snapshot = buildProposedAccommodationSnapshot()

    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = snapshot,
      accommodationTypeEntity = null,
      accommodationStatusEntity = buildAccommodationStatusEntity(
        code = "PR",
        name = "Proposed",
      ),
    )

    assertThat(entity.accommodationTypeId).isNull()
  }

  @Test
  fun `toEntity maps null CPR address id`() {
    val snapshot = buildProposedAccommodationSnapshot(
      cprAddressId = null,
    )

    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = snapshot,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )

    assertThat(entity.cprAddressId).isNull()
  }

  @ParameterizedTest
  @EnumSource(VerificationStatus::class)
  fun `toEntity maps verificationStatus enum values correctly`(
    verificationStatus: VerificationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(verificationStatus = verificationStatus),
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
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
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(nextAccommodationStatus.name))
  }

  @Test
  fun `merge should copy all fields from snapshot to entity`() {
    val entityId = UUID.randomUUID()
    val caseID = UUID.randomUUID()
    val cprAddressId = UUID.randomUUID()
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      id = entityId,
      caseId = caseID,
      cprAddressId = UUID.randomUUID(),
    )
    val accommodationTypeEntity = buildAccommodationTypeEntity()
    val accommodationStatusEntity = buildAccommodationStatusEntity()
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
      cprAddressId = cprAddressId,
      notes = mutableListOf(newNote1, newNote2, preExistingNote),
    )
    val merged = ProposedAccommodationMapper.merge(
      snapshot,
      proposedAccommodationEntity,
      accommodationTypeEntity,
      accommodationStatusEntity,
    )

    assertThat(merged.id).isEqualTo(entityId)
    assertThat(merged.caseId).isEqualTo(caseID)
    assertThat(merged.cprAddressId).isEqualTo(snapshot.cprAddressId)
    assertThat(merged.accommodationTypeId).isEqualTo(accommodationTypeEntity.id)
    assertThat(merged.accommodationStatusId).isEqualTo(accommodationStatusEntity.id)
    assertThat(merged.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(snapshot.verificationStatus.name))
    assertThat(merged.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    assertThat(merged.startDate).isEqualTo(snapshot.startDate)
    assertThat(merged.endDate).isEqualTo(snapshot.endDate)
    assertThat(merged.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(merged.subBuildingName).isEqualTo(snapshot.address.subBuildingName)
    assertThat(merged.buildingName).isEqualTo(snapshot.address.buildingName)
    assertThat(merged.buildingNumber).isEqualTo(snapshot.address.buildingNumber)
    assertThat(merged.thoroughfareName).isEqualTo(snapshot.address.thoroughfareName)
    assertThat(merged.dependentLocality).isEqualTo(snapshot.address.dependentLocality)
    assertThat(merged.postTown).isEqualTo(snapshot.address.postTown)
    assertThat(merged.county).isEqualTo(snapshot.address.county)
    assertThat(merged.country).isNull()
    assertThat(merged.uprn).isEqualTo(snapshot.address.uprn)
    assertThat(merged.notes).hasSize(3)
    assertThat(merged.notes.first().note).isEqualTo(preExistingNoteEntity.note)
    assertThat(merged.notes[1].note).isEqualTo(newNote1.note)
    assertThat(merged.notes[2].note).isEqualTo(newNote2.note)
  }

  @Test
  fun `merge should map all empty address fields to null`() {
    val snapshot = buildProposedAccommodationSnapshot(
      cprAddressId = UUID.randomUUID(),
      address = buildAccommodationAddressDetails(
        postcode = "SW1A 1AA",
        subBuildingName = "",
        buildingName = "",
        buildingNumber = "",
        thoroughfareName = "",
        dependentLocality = "",
        postTown = "",
        county = "",
        country = "",
        uprn = "",
      ),
    )
    val merged = ProposedAccommodationMapper.merge(
      snapshot,
      proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = UUID.randomUUID(),
        caseId = UUID.randomUUID(),
        cprAddressId = UUID.randomUUID(),
      ),
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )
    assertThat(merged.postcode).isEqualTo(snapshot.address.postcode)
    assertThat(merged.subBuildingName).isNull()
    assertThat(merged.buildingName).isNull()
    assertThat(merged.buildingNumber).isNull()
    assertThat(merged.thoroughfareName).isNull()
    assertThat(merged.dependentLocality).isNull()
    assertThat(merged.postTown).isNull()
    assertThat(merged.county).isNull()
    assertThat(merged.country).isNull()
    assertThat(merged.uprn).isNull()
  }

  @Test
  fun `merge should map null accommodation status entity`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )
    val snapshot = buildProposedAccommodationSnapshot()

    val merged = ProposedAccommodationMapper.merge(
      snapshot = snapshot,
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = null,
    )

    assertThat(merged.accommodationStatusId).isNull()
  }

  @Test
  fun `merge should map null accommodation type entity`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      accommodationTypeEntity = buildAccommodationTypeEntity(),
    )
    val snapshot = buildProposedAccommodationSnapshot()

    val merged = ProposedAccommodationMapper.merge(
      snapshot = snapshot,
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = null,
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )

    assertThat(merged.accommodationTypeId).isNull()
  }

  @Test
  fun `merge should map null CPR address id`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      cprAddressId = UUID.randomUUID(),
    )
    val snapshot = buildProposedAccommodationSnapshot(
      cprAddressId = null,
    )

    val merged = ProposedAccommodationMapper.merge(
      snapshot = snapshot,
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )

    assertThat(merged.cprAddressId).isNull()
  }

  @Test
  fun `merge should copy accommodation source type verified and no fixed abode from snapshot to entity`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      accommodationSource = AccommodationSource.SAS,
      typeVerified = false,
      noFixedAbode = false,
    )
    val snapshot = buildProposedAccommodationSnapshot(
      accommodationSource = AccommodationSource.DELIUS,
      typeVerified = true,
      noFixedAbode = true,
    )

    val merged = ProposedAccommodationMapper.merge(
      snapshot = snapshot,
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
    )

    assertThat(merged.accommodationSource).isEqualTo(snapshot.accommodationSource)
    assertThat(merged.typeVerified).isEqualTo(snapshot.typeVerified)
    assertThat(merged.noFixedAbode).isEqualTo(snapshot.noFixedAbode)
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val currentAccommodation = buildAccommodationSummaryDto(
      type = buildAccommodationTypeDto(
        code = "A02",
        description = "Approved Premises",
      ),
      status = buildAccommodationStatusDto(
        code = "M",
        description = "Main",
      ),
    )
    val accommodationTypeEntity = buildAccommodationTypeEntity(
      code = "A07B", // Friends and Family (settled)
    )
    val accommodationStatusEntity = buildAccommodationStatusEntity(
      code = "PR",
      name = "Proposed",
    )
    val cprAddressId = UUID.randomUUID()
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      cprAddressId = cprAddressId,
      name = "Test Name",
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      postcode = "SW1A 1AA",
      subBuildingName = "Sub",
      buildingName = "Building",
      buildingNumber = "10",
      thoroughfareName = "Downing Street",
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

    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity,
      accommodationTypeEntity,
      accommodationStatusEntity,
      currentAccommodation,
    )
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(proposedAccommodationEntity.id)
    assertThat(snapshot.caseId).isEqualTo(proposedAccommodationEntity.caseId)
    assertThat(snapshot.cprAddressId).isEqualTo(proposedAccommodationEntity.cprAddressId)
    assertThat(snapshot.accommodationType!!.code).isEqualTo(accommodationTypeEntity.code)
    assertThat(snapshot.accommodationType.description).isEqualTo(accommodationTypeEntity.name)
    assertThat(snapshot.accommodationStatus!!.code).isEqualTo(accommodationStatusEntity.code)
    assertThat(snapshot.accommodationStatus.description).isEqualTo(accommodationStatusEntity.name)
    assertThat(snapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(snapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(snapshot.address.postcode).isEqualTo(proposedAccommodationEntity.postcode)
    assertThat(snapshot.address.subBuildingName).isEqualTo(proposedAccommodationEntity.subBuildingName)
    assertThat(snapshot.address.buildingName).isEqualTo(proposedAccommodationEntity.buildingName)
    assertThat(snapshot.address.buildingNumber).isEqualTo(proposedAccommodationEntity.buildingNumber)
    assertThat(snapshot.address.thoroughfareName).isEqualTo(proposedAccommodationEntity.thoroughfareName)
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
    assertThat(snapshot.accommodationSource).isEqualTo(proposedAccommodationEntity.accommodationSource)
    assertThat(snapshot.typeVerified).isEqualTo(proposedAccommodationEntity.typeVerified)
    assertThat(snapshot.noFixedAbode).isEqualTo(proposedAccommodationEntity.noFixedAbode)
  }

  @Test
  fun `toAggregate maps null accommodation status entity`() {
    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity = buildProposedAccommodationEntity(),
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = null,
      currentAccommodation = null,
    )
    val snapshot = aggregate.snapshot()
    assertThat(snapshot.accommodationStatus).isNull()
  }

  @Test
  fun `toAggregate maps null accommodation type entity`() {
    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity = buildProposedAccommodationEntity(),
      accommodationTypeEntity = null,
      accommodationStatusEntity = buildAccommodationStatusEntity(
        code = "PR",
        name = "Proposed",
      ),
      currentAccommodation = null,
    )
    val snapshot = aggregate.snapshot()
    assertThat(snapshot.accommodationType).isNull()
  }

  @Test
  fun `toAggregate maps null CPR address id`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      cprAddressId = null,
    )

    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
      currentAccommodation = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.cprAddressId).isNull()
  }

  @Test
  fun `toAggregate maps accommodation source type verified and no fixed abode`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity(
      accommodationSource = AccommodationSource.DELIUS,
      typeVerified = true,
      noFixedAbode = true,
    )

    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = buildAccommodationTypeEntity(),
      accommodationStatusEntity = buildAccommodationStatusEntity(),
      currentAccommodation = null,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.accommodationSource).isEqualTo(proposedAccommodationEntity.accommodationSource)
    assertThat(snapshot.typeVerified).isEqualTo(proposedAccommodationEntity.typeVerified)
    assertThat(snapshot.noFixedAbode).isEqualTo(proposedAccommodationEntity.noFixedAbode)
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
    assertThat(dto.accommodationType).isEqualTo(snapshot.accommodationType)
    assertThat(dto.verificationStatus).isEqualTo(snapshot.verificationStatus)
    assertThat(dto.nextAccommodationStatus).isEqualTo(snapshot.nextAccommodationStatus)
    assertThat(dto.address).isEqualTo(snapshot.address)
    assertThat(dto.createdBy).isEqualTo(createdBy)
    assertThat(dto.createdAt).isEqualTo(createdAt)
  }
}
