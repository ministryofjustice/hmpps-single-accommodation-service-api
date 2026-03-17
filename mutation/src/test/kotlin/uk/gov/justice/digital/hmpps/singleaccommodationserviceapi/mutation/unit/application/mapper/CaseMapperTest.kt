package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

class CaseMapperTest {

  @Test
  fun `toAggregate maps all fields correctly`() {
    val caseEntity = buildCaseEntity(tier = TierScore.A1)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertAll(
      { assertThat(snapshot.id).isEqualTo(caseEntity.id) },
      {
        assertThat(snapshot.caseIdentifiers)
          .isEqualTo(
            caseEntity.caseIdentifiers.map {
              CaseAggregate.CaseIdentifier(
                it.id,
                it.identifier,
                it.identifierType,
              )
            }.toSet(),
          )
      },
      {
        assertThat(snapshot.tier).isNotNull.isEqualTo(caseEntity.tierScore)
      },
    )
  }

  @Test
  fun `toAggregate maps nullable tier enum fields as null`() {
    val caseEntity = buildCaseEntity(tier = null)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.tier).isNull()
  }
}
