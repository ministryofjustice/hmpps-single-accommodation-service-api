package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

class CaseAggregateTest {
  private val id = UUID.randomUUID()

  @Test
  fun `hydrate loads aggregate correctly`() {
    val tierScore = TierScore.A1

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tier).isEqualTo(tierScore)
      },
    )
  }

  @Test
  fun `createNew prepares aggregate`() {
    val newAggregate = CaseAggregate.hydrateNew()
    assertThat(newAggregate.snapshot().id).isNotNull()
    assertThat(newAggregate.snapshot().tier).isNull()
  }

  @Test
  fun `updateTier() should update TierScore`() {
    val aggregate = CaseAggregate.hydrateNew()

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tier).isNull()

    aggregate.updateTier(TierScore.A1)
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tier).isEqualTo(TierScore.A1)
  }
}
