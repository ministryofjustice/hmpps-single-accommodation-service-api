package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

class CaseAggregateTest {

  @Test
  fun `should upsertTier`() {
    val id = UUID.randomUUID()
    val crn = "ABC1234"
    val tier = TierScore.A1
    val aggregate = CaseAggregate.createNew(
      id = id,
      crn = crn,
    )
    aggregate.upsertTier(
      newTier = tier,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.tier!!.name).isEqualTo("tier!!!!")
  }
}
