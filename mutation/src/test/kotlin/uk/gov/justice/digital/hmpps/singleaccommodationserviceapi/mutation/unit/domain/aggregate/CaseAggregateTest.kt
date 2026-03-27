package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.util.UUID

class CaseAggregateTest {

  private val id = UUID.randomUUID()
  private val crn = UUID.randomUUID().toString()
  private val caseIdentifiers = mutableSetOf(
    CaseAggregate.CaseIdentifier(UUID.randomUUID(), crn, IdentifierType.CRN),
  )

  @Test
  fun `hydrate loads aggregate correctly`() {
    val tierScore = TierScore.A1

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
      caseIdentifiers = caseIdentifiers,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tier).isEqualTo(tierScore)
        assertThat(it.caseIdentifiers).isEqualTo(caseIdentifiers)
      },
    )
  }

  @Test
  fun `createNew prepares aggregate`() {
    val newAggregate = CaseAggregate.createNew(id = id, caseIdentifiers = caseIdentifiers)
    assertThat(newAggregate.snapshot()).isEqualTo(
      CaseSnapshot(
        id = id,
        caseIdentifiers = caseIdentifiers,
        tier = null,
      ),
    )
  }

  @Test
  fun `updateTier() should update TierScore`() {
    val aggregate = CaseAggregate.createNew(
      id = id,
      caseIdentifiers = caseIdentifiers,
    )

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tier).isNull()

    aggregate.updateTier(TierScore.A1)
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tier).isEqualTo(TierScore.A1)
  }
}
