package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseSnapshot

class CaseMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildCaseSnapshot(tier = TierScore.A1S)
    val entity = CaseMapper.toEntity(snapshot)

    Assertions.assertThat(entity.id).isEqualTo(snapshot.id)
    Assertions.assertThat(entity.crn).isEqualTo(snapshot.crn)
    Assertions.assertThat(entity.tier).isEqualTo(snapshot.tier)
  }

  @Test
  fun `toEntity maps nullable tier enum fields as null`() {
    val entity = CaseMapper.toEntity(
      snapshot = buildCaseSnapshot(
        tier = null,
      ),
    )
    Assertions.assertThat(entity.tier).isNull()
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

  @Test
  fun `toAggregate maps all fields correctly`() {
    val caseEntity = buildCaseEntity(tier = TierScore.A1)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.id).isEqualTo(caseEntity.id)
    Assertions.assertThat(snapshot.crn).isEqualTo(caseEntity.crn)
    Assertions.assertThat(snapshot.tier).isEqualTo(caseEntity.tier)
  }

  @Test
  fun `toAggregate maps nullable tier enum fields as null`() {
    val caseEntity = buildCaseEntity(tier = null)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.tier).isEqualTo(caseEntity.tier)
  }

  @ParameterizedTest
  @EnumSource(TierScore::class)
  fun `toAggregate maps tier enum values correctly`(
    tier: TierScore,
  ) {
    val caseEntity = buildCaseEntity(tier = tier)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    Assertions.assertThat(snapshot.tier).isEqualTo(caseEntity.tier)
  }
}
