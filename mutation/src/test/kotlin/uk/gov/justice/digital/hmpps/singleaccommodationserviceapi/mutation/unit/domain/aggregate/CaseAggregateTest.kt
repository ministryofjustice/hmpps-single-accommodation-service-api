package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

class CaseAggregateTest {
  private val id = UUID.randomUUID()
  private val cas1ApplicationId = UUID.randomUUID()

  @Test
  fun `hydrate loads aggregate correctly`() {
    val tierScore = TierScore.A1
    val cas1ApplicationId = UUID.randomUUID()
    val cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    val cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED
    val cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
      cas1ApplicationId = cas1ApplicationId,
      cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tierScore).isEqualTo(tierScore)
        assertThat(it.cas1ApplicationId).isEqualTo(cas1ApplicationId)
        assertThat(it.cas1ApplicationApplicationStatus).isEqualTo(cas1ApplicationApplicationStatus)
        assertThat(it.cas1ApplicationRequestForPlacementStatus).isEqualTo(cas1ApplicationRequestForPlacementStatus)
        assertThat(it.cas1ApplicationPlacementStatus).isEqualTo(cas1ApplicationPlacementStatus)
      },
    )
  }

  @Test
  fun `createNew prepares aggregate`() {
    val newAggregate = CaseAggregate.hydrateNew()
    assertThat(newAggregate.snapshot().id).isNotNull()
    assertThat(newAggregate.snapshot().tierScore).isNull()
    assertThat(newAggregate.snapshot().cas1ApplicationId).isNull()
    assertThat(newAggregate.snapshot().cas1ApplicationApplicationStatus).isNull()
    assertThat(newAggregate.snapshot().cas1ApplicationRequestForPlacementStatus).isNull()
    assertThat(newAggregate.snapshot().cas1ApplicationPlacementStatus).isNull()
  }

  @Test
  fun `updateTier() should update TierScore`() {
    val aggregate = CaseAggregate.hydrateNew()

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tierScore).isNull()

    aggregate.updateTier(TierScore.A1)
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo(TierScore.A1)
  }

  @Test
  fun `upsertCase() should set all fields onto the aggregate`() {
    val aggregate = CaseAggregate.hydrateNew()

    val beforeUpdate = aggregate.snapshot()
    assertThat(beforeUpdate.tierScore).isNull()
    assertThat(beforeUpdate.cas1ApplicationId).isNull()
    assertThat(beforeUpdate.cas1ApplicationApplicationStatus).isNull()
    assertThat(beforeUpdate.cas1ApplicationRequestForPlacementStatus).isNull()
    assertThat(beforeUpdate.cas1ApplicationPlacementStatus).isNull()

    aggregate.upsertCase(
      tierScore = TierScore.A1,
      cas1ApplicationId = cas1ApplicationId,
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.ARRIVED,
    )
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo(TierScore.A1)
    assertThat(afterUpdate.cas1ApplicationId).isEqualTo(cas1ApplicationId)
    assertThat(afterUpdate.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.PLACEMENT_ALLOCATED)
    assertThat(afterUpdate.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.PLACEMENT_BOOKED)
    assertThat(afterUpdate.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.ARRIVED)
  }
}
