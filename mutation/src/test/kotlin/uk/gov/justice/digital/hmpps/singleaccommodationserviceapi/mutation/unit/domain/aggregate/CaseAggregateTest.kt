package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.time.LocalDate
import java.util.UUID

class CaseAggregateTest {
  private val id = UUID.randomUUID()

  @Test
  fun `hydrate loads aggregate correctly`() {
    val tierScore = "A1"
    val dateOfBirth = LocalDate.of(1990, 1, 1)

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
      hasSyncedCprProposedAccommodation = true,
      firstName = "First",
      lastName = "Last",
      dateOfBirth = dateOfBirth,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tierScore).isEqualTo(tierScore)
        assertThat(it.hasSyncedCprProposedAccommodation).isTrue()
        assertThat(it.firstName).isEqualTo("First")
        assertThat(it.lastName).isEqualTo("Last")
        assertThat(it.dateOfBirth).isEqualTo(dateOfBirth)
      },
    )
  }

  @Test
  fun `createNew prepares aggregate`() {
    val newAggregate = CaseAggregate.hydrateNew()
    assertThat(newAggregate.snapshot().id).isNotNull()
    assertThat(newAggregate.snapshot().tierScore).isNull()
    assertThat(newAggregate.snapshot().hasSyncedCprProposedAccommodation).isFalse()
    assertThat(newAggregate.snapshot().firstName).isNull()
    assertThat(newAggregate.snapshot().lastName).isNull()
    assertThat(newAggregate.snapshot().dateOfBirth).isNull()
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

    val dateOfBirth = LocalDate.of(1985, 6, 15)

    aggregate.upsertCase(
      tierScore = "A1",
      firstName = "First",
      lastName = "Last",
      dateOfBirth = dateOfBirth,
    )
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo("A1")
    assertThat(afterUpdate.firstName).isEqualTo("First")
    assertThat(afterUpdate.lastName).isEqualTo("Last")
    assertThat(afterUpdate.dateOfBirth).isEqualTo(dateOfBirth)
  }

  @Test
  fun `upsertCase() should set null person detail fields onto the aggregate when not provided`() {
    val aggregate = CaseAggregate.hydrateNew()

    aggregate.upsertCase(
      tierScore = "A1",
    )
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.firstName).isNull()
    assertThat(afterUpdate.lastName).isNull()
    assertThat(afterUpdate.dateOfBirth).isNull()
  }
}
