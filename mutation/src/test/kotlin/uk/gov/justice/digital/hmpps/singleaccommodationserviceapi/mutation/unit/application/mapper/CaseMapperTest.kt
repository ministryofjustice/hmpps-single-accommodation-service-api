package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseSnapshot
import java.util.UUID

class CaseMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildCaseSnapshot(
      tier = TierScore.A1S,
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
    )
    val entity = CaseMapper.toEntity(snapshot)

    Assertions.assertThat(entity.id).isEqualTo(snapshot.id)
    Assertions.assertThat(entity.crn).isEqualTo(snapshot.crn)
    Assertions.assertThat(entity.tier).isEqualTo(snapshot.tier)
    Assertions.assertThat(entity.cas1ApplicationId).isEqualTo(snapshot.cas1ApplicationId)
    Assertions.assertThat(entity.cas1ApplicationApplicationStatus).isEqualTo(snapshot.cas1ApplicationApplicationStatus)
    Assertions.assertThat(entity.cas1ApplicationRequestForPlacementStatus).isEqualTo(snapshot.cas1ApplicationRequestForPlacementStatus)
    Assertions.assertThat(entity.cas1ApplicationPlacementStatus).isEqualTo(snapshot.cas1ApplicationPlacementStatus)
  }

  @Test
  fun `toEntity maps nullable fields as null`() {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(
        tier = null,
        cas1ApplicationId = null,
        cas1ApplicationApplicationStatus = null,
        cas1ApplicationRequestForPlacementStatus = null,
        cas1ApplicationPlacementStatus = null,
      ),
    )

    Assertions.assertThat(entity.tier).isNull()
    Assertions.assertThat(entity.cas1ApplicationId).isNull()
    Assertions.assertThat(entity.cas1ApplicationApplicationStatus).isNull()
    Assertions.assertThat(entity.cas1ApplicationRequestForPlacementStatus).isNull()
    Assertions.assertThat(entity.cas1ApplicationPlacementStatus).isNull()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class)
  fun `toEntity maps tier enum values correctly`(
    tier: TierScore,
  ) {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(tier = tier),
    )

    Assertions.assertThat(entity.tier).isEqualTo(TierScore.valueOf(tier.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1ApplicationStatus::class)
  fun `toEntity maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus,
  ) {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus),
    )

    Assertions.assertThat(entity.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.valueOf(cas1ApplicationApplicationStatus.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1RequestForPlacementStatus::class)
  fun `toEntity maps cas1ApplicationRequestForPlacementStatus enum values correctly`(
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus,
  ) {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus),
    )

    Assertions.assertThat(entity.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.valueOf(cas1ApplicationRequestForPlacementStatus.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1PlacementStatus::class)
  fun `toEntity maps cas1ApplicationPlacementStatus enum values correctly`(
    cas1ApplicationPlacementStatus: Cas1PlacementStatus,
  ) {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus),
    )

    Assertions.assertThat(entity.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.valueOf(cas1ApplicationPlacementStatus.name))
  }

  @Test
  fun `toAggregate maps all fields correctly`() {
    val caseEntity = buildCaseEntity(
      tier = TierScore.A1S,
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
    )
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.id).isEqualTo(caseEntity.id)
    Assertions.assertThat(snapshot.crn).isEqualTo(caseEntity.crn)
    Assertions.assertThat(snapshot.tier).isEqualTo(caseEntity.tier)
    Assertions.assertThat(snapshot.cas1ApplicationId).isEqualTo(caseEntity.cas1ApplicationId)
    Assertions.assertThat(snapshot.cas1ApplicationApplicationStatus).isEqualTo(caseEntity.cas1ApplicationApplicationStatus)
    Assertions.assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isEqualTo(caseEntity.cas1ApplicationRequestForPlacementStatus)
    Assertions.assertThat(snapshot.cas1ApplicationPlacementStatus).isEqualTo(caseEntity.cas1ApplicationPlacementStatus)
  }

  @Test
  fun `toAggregate maps nullable fields as null`() {
    val caseEntity = buildCaseEntity(
      tier = null,
      cas1ApplicationId = null,
      cas1ApplicationApplicationStatus = null,
      cas1ApplicationRequestForPlacementStatus = null,
      cas1ApplicationPlacementStatus = null,
    )
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.tier).isNull()
    Assertions.assertThat(snapshot.cas1ApplicationId).isNull()
    Assertions.assertThat(snapshot.cas1ApplicationApplicationStatus).isNull()
    Assertions.assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isNull()
    Assertions.assertThat(snapshot.cas1ApplicationPlacementStatus).isNull()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class)
  fun `toAggregate maps tier enum values correctly`(
    tier: TierScore,
  ) {
    val caseEntity = buildCaseEntity(tier = tier)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.tier).isEqualTo(TierScore.valueOf(caseEntity.tier!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1ApplicationStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.valueOf(caseEntity.cas1ApplicationApplicationStatus!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1RequestForPlacementStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.valueOf(caseEntity.cas1ApplicationRequestForPlacementStatus!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1PlacementStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationPlacementStatus: Cas1PlacementStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.valueOf(caseEntity.cas1ApplicationPlacementStatus!!.name))
  }
}
