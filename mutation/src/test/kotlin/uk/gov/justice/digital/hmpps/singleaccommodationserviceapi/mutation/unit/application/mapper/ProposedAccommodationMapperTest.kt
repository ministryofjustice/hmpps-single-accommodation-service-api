package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildProposedAccommodationSnapshot
import java.time.temporal.ChronoUnit

class ProposedAccommodationMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val entity = ProposedAccommodationMapper.toEntity(snapshot)

    Assertions.assertThat(entity.id).isEqualTo(snapshot.id)
    Assertions.assertThat(entity.crn).isEqualTo(snapshot.crn)
    Assertions.assertThat(entity.name).isEqualTo(snapshot.name)
    Assertions.assertThat(entity.arrangementType).isEqualTo(AccommodationArrangementType.valueOf(snapshot.arrangementType.name))
    Assertions.assertThat(entity.arrangementSubType).isEqualTo(AccommodationArrangementSubType.valueOf(snapshot.arrangementSubType!!.name))
    Assertions.assertThat(entity.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    Assertions.assertThat(entity.settledType).isEqualTo(AccommodationSettledType.valueOf(snapshot.settledType.name))
    Assertions.assertThat(entity.verificationStatus).isEqualTo(VerificationStatus.valueOf(snapshot.verificationStatus.name))
    Assertions.assertThat(entity.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name))
    Assertions.assertThat(entity.offenderReleaseType).isEqualTo(OffenderReleaseType.valueOf(snapshot.offenderReleaseType!!.name))
    Assertions.assertThat(entity.startDate).isEqualTo(snapshot.startDate)
    Assertions.assertThat(entity.endDate).isEqualTo(snapshot.endDate)
    Assertions.assertThat(entity.postcode).isEqualTo(snapshot.address.postcode)
    Assertions.assertThat(entity.subBuildingName).isEqualTo(snapshot.address.subBuildingName)
    Assertions.assertThat(entity.buildingName).isEqualTo(snapshot.address.buildingName)
    Assertions.assertThat(entity.buildingNumber).isEqualTo(snapshot.address.buildingNumber)
    Assertions.assertThat(entity.throughfareName).isEqualTo(snapshot.address.thoroughfareName)
    Assertions.assertThat(entity.dependentLocality).isEqualTo(snapshot.address.dependentLocality)
    Assertions.assertThat(entity.postTown).isEqualTo(snapshot.address.postTown)
    Assertions.assertThat(entity.county).isEqualTo(snapshot.address.county)
    Assertions.assertThat(entity.country).isEqualTo(snapshot.address.country)
    Assertions.assertThat(entity.uprn).isEqualTo(snapshot.address.uprn)
    Assertions.assertThat(entity.createdAt).isEqualTo(snapshot.createdAt)
    Assertions.assertThat(entity.lastUpdatedAt).isEqualTo(snapshot.lastUpdatedAt)
  }

  @Test
  fun `toEntity maps nullable enum fields as null`() {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(
        arrangementSubType = null,
        offenderReleaseType = null,
      ),
    )
    Assertions.assertThat(entity.arrangementSubType).isNull()
    Assertions.assertThat(entity.offenderReleaseType).isNull()
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType::class)
  fun `toEntity maps arrangementType enum values correctly`(
    arrangementType: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(arrangementType = arrangementType),
    )

    Assertions.assertThat(entity.arrangementType).isEqualTo(AccommodationArrangementType.valueOf(arrangementType.name))
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType::class)
  fun `toEntity maps arrangementSubType enum values correctly`(
    arrangementSubType: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(arrangementSubType = arrangementSubType),
    )
    Assertions.assertThat(entity.arrangementSubType).isEqualTo(AccommodationArrangementSubType.valueOf(arrangementSubType.name))
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType::class)
  fun `toEntity maps settledType enum values correctly`(
    settledType: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(settledType = settledType),
    )
    Assertions.assertThat(entity.settledType).isEqualTo(AccommodationSettledType.valueOf(settledType.name))
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus::class)
  fun `toEntity maps verificationStatus enum values correctly`(
    verificationStatus: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(verificationStatus = verificationStatus),
    )
    Assertions.assertThat(entity.verificationStatus).isEqualTo(VerificationStatus.valueOf(verificationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus::class)
  fun `toEntity maps nextAccommodationStatus enum values correctly`(
    nextAccommodationStatus: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(nextAccommodationStatus = nextAccommodationStatus),
    )
    Assertions.assertThat(entity.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.valueOf(nextAccommodationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType::class)
  fun `toEntity maps offenderReleaseType enum values correctly`(
    offenderReleaseType: uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildProposedAccommodationSnapshot(offenderReleaseType = offenderReleaseType),
    )
    Assertions.assertThat(entity.offenderReleaseType).isEqualTo(OffenderReleaseType.valueOf(offenderReleaseType.name))
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildProposedAccommodationSnapshot()
    val dto = ProposedAccommodationMapper.toDto(snapshot)
    Assertions.assertThat(dto.id).isEqualTo(snapshot.id)
    Assertions.assertThat(dto.name).isEqualTo(snapshot.name)
    Assertions.assertThat(dto.arrangementType).isEqualTo(snapshot.arrangementType)
    Assertions.assertThat(dto.arrangementSubType).isEqualTo(snapshot.arrangementSubType)
    Assertions.assertThat(dto.arrangementSubTypeDescription).isEqualTo(snapshot.arrangementSubTypeDescription)
    Assertions.assertThat(dto.settledType).isEqualTo(snapshot.settledType)
    Assertions.assertThat(dto.verificationStatus).isEqualTo(snapshot.verificationStatus)
    Assertions.assertThat(dto.nextAccommodationStatus).isEqualTo(snapshot.nextAccommodationStatus)
    Assertions.assertThat(dto.offenderReleaseType).isEqualTo(snapshot.offenderReleaseType)
    Assertions.assertThat(dto.startDate).isEqualTo(snapshot.startDate)
    Assertions.assertThat(dto.endDate).isEqualTo(snapshot.endDate)
    Assertions.assertThat(dto.address).isEqualTo(snapshot.address)
    Assertions.assertThat(dto.createdAt).isEqualTo(snapshot.createdAt.truncatedTo(ChronoUnit.SECONDS))
  }
}