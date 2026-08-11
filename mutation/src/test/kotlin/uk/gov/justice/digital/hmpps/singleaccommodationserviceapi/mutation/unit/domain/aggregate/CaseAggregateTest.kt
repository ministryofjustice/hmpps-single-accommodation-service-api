package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

class CaseAggregateTest {
  private val id = UUID.randomUUID()

  @Test
  fun `hydrate loads aggregate correctly`() {
    val tierScore = "A1"

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
      hasSyncedCprProposedAccommodation = true,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tierScore).isEqualTo(tierScore)
        assertThat(it.hasSyncedCprProposedAccommodation).isTrue()
      },
    )
  }

  @Test
  fun `createNew prepares aggregate`() {
    val newAggregate = CaseAggregate.hydrateNew()
    assertThat(newAggregate.snapshot().id).isNotNull()
    assertThat(newAggregate.snapshot().tierScore).isNull()
    assertThat(newAggregate.snapshot().hasSyncedCprProposedAccommodation).isFalse()
  }

  @Test
  fun `markCaseAsSyncedWithCprProposedAccommodation() should set hasSyncedCprProposedAccommodation to true`() {
    val aggregate = CaseAggregate.hydrateNew()

    assertThat(aggregate.snapshot().hasSyncedCprProposedAccommodation).isFalse()

    aggregate.markCaseAsSyncedWithCprProposedAccommodation()

    assertThat(aggregate.snapshot().hasSyncedCprProposedAccommodation).isTrue()
  }

  @Test
  fun `updateTier() should update tier`() {
    val aggregate = CaseAggregate.hydrateNew()

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tierScore).isNull()

    aggregate.updateTier("A1")
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo("A1")
  }

  @Test
  fun `upsertCase() should set all fields onto the aggregate`() {
    val aggregate = CaseAggregate.hydrateNew()

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tierScore).isNull()

    aggregate.upsertCase(
      tierScore = "A1",
    )
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo("A1")
  }
}
