package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import java.time.Instant
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factory.buildProposedAccommodationSnapshot
import java.time.temporal.ChronoUnit
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as EntityAccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as EntityAccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as EntityAccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as EntityOffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val entity = ProposedAccommodationMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.name).isEqualTo(snapshot.name)
    assertThat(entity.arrangementType).isEqualTo(EntityAccommodationArrangementType.valueOf(snapshot.arrangementType.name))
    assertThat(entity.arrangementSubType).isEqualTo(EntityAccommodationArrangementSubType.valueOf(snapshot.arrangementSubType!!.name))
    assertThat(entity.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    assertThat(entity.settledType).isEqualTo(EntityAccommodationSettledType.valueOf(snapshot.settledType.name))
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(snapshot.verificationStatus.name))
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    assertThat(entity.offenderReleaseType).isEqualTo(EntityOffenderReleaseType.valueOf(snapshot.offenderReleaseType!!.name))
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
    assertThat(entity.createdAt).isEqualTo(snapshot.createdAt)
    assertThat(entity.lastUpdatedAt).isEqualTo(snapshot.lastUpdatedAt)
  }

  @Test
  fun `toEntity maps nullable enum fields as null`() {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(
        arrangementSubType = null,
        offenderReleaseType = null,
      ),
    )
    assertThat(entity.arrangementSubType).isNull()
    assertThat(entity.offenderReleaseType).isNull()
  }

  @ParameterizedTest
  @EnumSource(AccommodationArrangementType::class)
  fun `toEntity maps arrangementType enum values correctly`(
    arrangementType: AccommodationArrangementType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(arrangementType = arrangementType)
    )

    assertThat(entity.arrangementType).isEqualTo(EntityAccommodationArrangementType.valueOf(arrangementType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationArrangementSubType::class)
  fun `toEntity maps arrangementSubType enum values correctly`(
    arrangementSubType: AccommodationArrangementSubType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(arrangementSubType = arrangementSubType)
    )
    assertThat(entity.arrangementSubType).isEqualTo(EntityAccommodationArrangementSubType.valueOf(arrangementSubType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationSettledType::class)
  fun `toEntity maps settledType enum values correctly`(
    settledType: AccommodationSettledType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(settledType = settledType)
    )
    assertThat(entity.settledType).isEqualTo(EntityAccommodationSettledType.valueOf(settledType.name))
  }

  @ParameterizedTest
  @EnumSource(VerificationStatus::class)
  fun `toEntity maps verificationStatus enum values correctly`(
    verificationStatus: VerificationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(verificationStatus = verificationStatus)
    )
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(verificationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(NextAccommodationStatus::class)
  fun `toEntity maps nextAccommodationStatus enum values correctly`(
    nextAccommodationStatus: NextAccommodationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(nextAccommodationStatus = nextAccommodationStatus)
    )
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(nextAccommodationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(OffenderReleaseType::class)
  fun `toEntity maps offenderReleaseType enum values correctly`(
    offenderReleaseType: OffenderReleaseType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(offenderReleaseType = offenderReleaseType),
    )
    assertThat(entity.offenderReleaseType).isEqualTo(EntityOffenderReleaseType.valueOf(offenderReleaseType.name))
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val entity = buildProposedAccommodationEntity(
      name = "Test Name",
      arrangementType = EntityAccommodationArrangementType.PRIVATE,
      arrangementSubType = EntityAccommodationArrangementSubType.FRIENDS_OR_FAMILY,
      arrangementSubTypeDescription = null,
      settledType = EntityAccommodationSettledType.SETTLED,
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      offenderReleaseType = EntityOffenderReleaseType.REMAND,
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
      createdAt = Instant.parse("2025-06-01T10:00:00Z"),
      lastUpdatedAt = Instant.parse("2025-06-02T10:00:00Z"),
    )

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(entity.id)
    assertThat(snapshot.crn).isEqualTo(entity.crn)
    assertThat(snapshot.name).isEqualTo(entity.name)
    assertThat(snapshot.arrangementType).isEqualTo(AccommodationArrangementType.PRIVATE)
    assertThat(snapshot.arrangementSubType).isEqualTo(AccommodationArrangementSubType.FRIENDS_OR_FAMILY)
    assertThat(snapshot.arrangementSubTypeDescription).isEqualTo(entity.arrangementSubTypeDescription)
    assertThat(snapshot.settledType).isEqualTo(AccommodationSettledType.SETTLED)
    assertThat(snapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(snapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(snapshot.offenderReleaseType).isEqualTo(OffenderReleaseType.REMAND)
    assertThat(snapshot.address.postcode).isEqualTo(entity.postcode)
    assertThat(snapshot.address.subBuildingName).isEqualTo(entity.subBuildingName)
    assertThat(snapshot.address.buildingName).isEqualTo(entity.buildingName)
    assertThat(snapshot.address.buildingNumber).isEqualTo(entity.buildingNumber)
    assertThat(snapshot.address.thoroughfareName).isEqualTo(entity.throughfareName)
    assertThat(snapshot.address.dependentLocality).isEqualTo(entity.dependentLocality)
    assertThat(snapshot.address.postTown).isEqualTo(entity.postTown)
    assertThat(snapshot.address.county).isEqualTo(entity.county)
    assertThat(snapshot.address.country).isEqualTo(entity.country)
    assertThat(snapshot.address.uprn).isEqualTo(entity.uprn)
    assertThat(snapshot.startDate).isEqualTo(entity.startDate)
    assertThat(snapshot.endDate).isEqualTo(entity.endDate)
    assertThat(snapshot.createdAt).isEqualTo(entity.createdAt)
    assertThat(snapshot.lastUpdatedAt).isEqualTo(entity.lastUpdatedAt)
  }

  @Test
  fun `toAggregate handles nullable enum fields`() {
    val entity = buildProposedAccommodationEntity(
      arrangementSubType = null,
      offenderReleaseType = null,
    )

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    val snapshot = aggregate.snapshot()

    assertThat(snapshot.arrangementSubType).isNull()
    assertThat(snapshot.offenderReleaseType).isNull()
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val dto = ProposedAccommodationMapper.toDto(snapshot)
    assertThat(dto.id).isEqualTo(snapshot.id)
    assertThat(dto.name).isEqualTo(snapshot.name)
    assertThat(dto.arrangementType).isEqualTo(snapshot.arrangementType)
    assertThat(dto.arrangementSubType).isEqualTo(snapshot.arrangementSubType)
    assertThat(dto.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    assertThat(dto.settledType).isEqualTo(snapshot.settledType)
    assertThat(dto.verificationStatus).isEqualTo(snapshot.verificationStatus)
    assertThat(dto.nextAccommodationStatus).isEqualTo(snapshot.nextAccommodationStatus)
    assertThat(dto.offenderReleaseType).isEqualTo(snapshot.offenderReleaseType)
    assertThat(dto.startDate).isEqualTo(snapshot.startDate)
    assertThat(dto.endDate).isEqualTo(snapshot.endDate)
    assertThat(dto.address).isEqualTo(snapshot.address)
    assertThat(dto.createdAt).isEqualTo(snapshot.createdAt.truncatedTo(ChronoUnit.SECONDS))
  }
}
