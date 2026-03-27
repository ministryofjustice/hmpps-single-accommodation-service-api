package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
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
    val caseEntity = buildCaseEntity(id = id, tier = null) { withCrn(identifier) }
    val caseAggregate = CaseAggregate.hydrateNew()

    val identifiersToMerge = mapOf(
      "NEW" to IdentifierType.PRISON_NUMBER,
      identifier to IdentifierType.CRN,
    )

    caseAggregate.updateTier(TierScore.A3S)

    val mergedEntity = CaseMapper.merge(
      entity = caseEntity,
      snapshot = caseAggregate.snapshot(),
      identifiers = identifiersToMerge,
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(2) },
      {
        assertThat(
          mergedEntity.caseIdentifiers
            .associate { it.identifier to it.identifierType },
        ).isEqualTo(identifiersToMerge)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo(TierScore.A3S) },
    )
  }

  @Test
  fun `merge snapshot into entity only adds missing identifiers`() {
    // set up the case entity with multiple identifiers
    val id = UUID.randomUUID()
    val identifier1 = "CRN1"
    val identifier2 = "CRN2"
    val identifier3 = "PRI1"
    val identifier4 = "PRI2"
    val caseEntity = buildCaseEntity(id = id, tier = null) {
      withCrn(identifier1)
      withPrisonNumber(identifier3)
    }

    // add the same identifiers onto the aggregate
    val caseAggregate = CaseAggregate.hydrateNew()

    // crete a set of identifiers containing existing and new
    val identifiersToMerge = mapOf(
      identifier1 to IdentifierType.CRN,
      identifier2 to IdentifierType.CRN,
      identifier3 to IdentifierType.PRISON_NUMBER,
      identifier4 to IdentifierType.PRISON_NUMBER,
    )

    caseAggregate.updateTier(TierScore.A3S)

    val mergedEntity = CaseMapper.merge(
      entity = caseEntity,
      snapshot = caseAggregate.snapshot(),
      identifiers = identifiersToMerge,
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(4) },
      {
        assertThat(
          mergedEntity.caseIdentifiers.associate { it.identifier to it.identifierType },
        ).isEqualTo(identifiersToMerge)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo(TierScore.A3S) },
    )
  }
}
