package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

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

  @Test
  fun `merge updates the entity correctly`() {
    val id = UUID.randomUUID()
    val identifier = UUID.randomUUID().toString()
    val caseEntity = buildCaseEntity(id = id, tier = null, identifier = identifier)
    val caseAggregate = CaseAggregate.createNew(
      id,
      mutableSetOf(
        CaseAggregate.CaseIdentifier(
          UUID.randomUUID(),
          identifier,
          IdentifierType.CRN,
        ),
      ),
    )

    val identifiersToMerge = setOf(
      "NEW" to IdentifierType.PRISON_NUMBER,
      identifier to IdentifierType.CRN,
    )

    caseAggregate.updateIdentifiers(identifiersToMerge)
    caseAggregate.updateTier(TierScore.A3S)

    val afterMerge = CaseMapper.merge(entity = caseEntity, snapshot = caseAggregate.snapshot())

    assertAll(
      { assertThat(afterMerge.caseIdentifiers).hasSize(2) },
      {
        val entityIdentifiiers = afterMerge.caseIdentifiers.map { it.identifier to it.identifierType }.toSet()
        assertThat(entityIdentifiiers).isEqualTo(identifiersToMerge)
      },
      { assertThat(afterMerge.tierScore).isEqualTo(TierScore.A3S) },
    )
  }
}
