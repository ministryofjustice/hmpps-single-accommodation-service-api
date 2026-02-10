package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factory.buildSnapshot
import java.time.temporal.ChronoUnit
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as EntityAccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as EntityAccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as EntityAccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as EntityOffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildSnapshot()
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
      snapshot = buildSnapshot(
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
      snapshot = buildSnapshot(arrangementType = arrangementType),
    )

    assertThat(entity.arrangementType).isEqualTo(EntityAccommodationArrangementType.valueOf(arrangementType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationArrangementSubType::class)
  fun `toEntity maps arrangementSubType enum values correctly`(
    arrangementSubType: AccommodationArrangementSubType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(arrangementSubType = arrangementSubType),
    )
    assertThat(entity.arrangementSubType).isEqualTo(EntityAccommodationArrangementSubType.valueOf(arrangementSubType.name))
  }

  @ParameterizedTest
  @EnumSource(AccommodationSettledType::class)
  fun `toEntity maps settledType enum values correctly`(
    settledType: AccommodationSettledType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(settledType = settledType),
    )
    assertThat(entity.settledType).isEqualTo(EntityAccommodationSettledType.valueOf(settledType.name))
  }

  @ParameterizedTest
  @EnumSource(VerificationStatus::class)
  fun `toEntity maps verificationStatus enum values correctly`(
    verificationStatus: VerificationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(verificationStatus = verificationStatus),
    )
    assertThat(entity.verificationStatus).isEqualTo(EntityVerificationStatus.valueOf(verificationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(NextAccommodationStatus::class)
  fun `toEntity maps nextAccommodationStatus enum values correctly`(
    nextAccommodationStatus: NextAccommodationStatus,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(nextAccommodationStatus = nextAccommodationStatus),
    )
    assertThat(entity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.valueOf(nextAccommodationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(OffenderReleaseType::class)
  fun `toEntity maps offenderReleaseType enum values correctly`(
    offenderReleaseType: OffenderReleaseType,
  ) {
    val entity = ProposedAccommodationMapper.toEntity(
      snapshot = buildSnapshot(offenderReleaseType = offenderReleaseType),
    )
    assertThat(entity.offenderReleaseType).isEqualTo(EntityOffenderReleaseType.valueOf(offenderReleaseType.name))
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
    assertThat(dto.verificationStatus).isEqualTo(snapshot.verificationStatus)
    assertThat(dto.nextAccommodationStatus).isEqualTo(snapshot.nextAccommodationStatus)
    assertThat(dto.offenderReleaseType).isEqualTo(snapshot.offenderReleaseType)
    assertThat(dto.startDate).isEqualTo(snapshot.startDate)
    assertThat(dto.endDate).isEqualTo(snapshot.endDate)
    assertThat(dto.address).isEqualTo(snapshot.address)
    assertThat(dto.createdAt).isEqualTo(snapshot.createdAt.truncatedTo(ChronoUnit.SECONDS))
  }
}
