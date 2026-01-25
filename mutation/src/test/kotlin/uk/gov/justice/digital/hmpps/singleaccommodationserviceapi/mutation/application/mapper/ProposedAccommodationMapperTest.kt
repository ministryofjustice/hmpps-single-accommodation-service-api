package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factory.buildSnapshot
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as AccommodationArrangementTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as AccommodationArrangementSubTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as AccommodationSettledTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatus as AccommodationStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as OffenderReleaseTypeInfra

class ProposedAccommodationMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildSnapshot()
    val entity = ProposedAccommodationMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.name).isEqualTo(snapshot.name)
    assertThat(entity.arrangementType).isEqualTo(AccommodationArrangementTypeInfra.valueOf(snapshot.arrangementType.name))
    assertThat(entity.arrangementSubType).isEqualTo(AccommodationArrangementSubTypeInfra.valueOf(snapshot.arrangementSubType!!.name))
    assertThat(entity.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    assertThat(entity.settledType).isEqualTo(AccommodationSettledTypeInfra.valueOf(snapshot.settledType.name))
    assertThat(entity.status).isEqualTo(AccommodationStatusInfra.valueOf(snapshot.status.name))
    assertThat(entity.offenderReleaseType).isEqualTo(OffenderReleaseTypeInfra.valueOf(snapshot.offenderReleaseType!!.name))
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
      snapshot = buildSnapshot(
        arrangementSubType = null,
        offenderReleaseType = null,
      )
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
      snapshot = buildSnapshot(arrangementType = arrangementType)
    )

    assertThat(entity.arrangementType).isEqualTo(AccommodationArrangementTypeInfra.valueOf(arrangementType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationArrangementSubType::class)
  fun `toEntity maps arrangementSubType enum values correctly`(
    arrangementSubType: AccommodationArrangementSubType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(arrangementSubType = arrangementSubType)
    )
    assertThat(entity.arrangementSubType).isEqualTo(AccommodationArrangementSubTypeInfra.valueOf(arrangementSubType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationSettledType::class)
  fun `toEntity maps settledType enum values correctly`(
    settledType: AccommodationSettledType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(settledType = settledType)
    )
    assertThat(entity.settledType).isEqualTo(AccommodationSettledTypeInfra.valueOf(settledType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationStatus::class)
  fun `toEntity maps status enum values correctly`(
    status: AccommodationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(status = status)
    )
    assertThat(entity.status).isEqualTo(AccommodationStatusInfra.valueOf(status.name))
  }

  @ParameterizedTest
  @EnumSource(OffenderReleaseType::class)
  fun `toEntity maps offenderReleaseType enum values correctly`(
    offenderReleaseType: OffenderReleaseType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(offenderReleaseType = offenderReleaseType)
    )
    assertThat(entity.offenderReleaseType).isEqualTo(OffenderReleaseTypeInfra.valueOf(offenderReleaseType.name))
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildSnapshot()
    val dto = ProposedAccommodationMapper.toDto(snapshot)
    assertThat(dto.id).isEqualTo(snapshot.id)
    assertThat(dto.name).isEqualTo(snapshot.name)
    assertThat(dto.arrangementType).isEqualTo(snapshot.arrangementType)
    assertThat(dto.arrangementSubType).isEqualTo(snapshot.arrangementSubType)
    assertThat(dto.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    assertThat(dto.settledType).isEqualTo(snapshot.settledType)
    assertThat(dto.status).isEqualTo(snapshot.status)
    assertThat(dto.offenderReleaseType).isEqualTo(snapshot.offenderReleaseType)
    assertThat(dto.startDate).isEqualTo(snapshot.startDate)
    assertThat(dto.endDate).isEqualTo(snapshot.endDate)
    assertThat(dto.address).isEqualTo(snapshot.address)
    assertThat(dto.createdAt).isEqualTo(snapshot.createdAt)
  }
}
