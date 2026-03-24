package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationOrchestrationDto
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
    assertThat(aggregateSnapshot.tier).isEqualTo(tier)
  }

  @Test
  fun `should upsertCase`() {
    val id = UUID.randomUUID()
    val crn = "ABC1234"
    val tier = TierScore.A1
    val aggregate = CaseAggregate.createNew(
      id = id,
      crn = crn,
    )
    val cas1ApplicationId = buildCas1Application()
    val freshCase = CaseApplicationOrchestrationDto(
      crn = crn,
      tier = buildTier(tier),
      cas1Application = cas1ApplicationId,
    )

    aggregate.upsertCase(
      freshCase = freshCase,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.tier).isEqualTo(tier)
    assertThat(aggregateSnapshot.cas1ApplicationId).isEqualTo(cas1ApplicationId.id)
    assertThat(aggregateSnapshot.cas1ApplicationApplicationStatus).isEqualTo(cas1ApplicationId.applicationStatus)
    assertThat(aggregateSnapshot.cas1ApplicationRequestForPlacementStatus).isEqualTo(cas1ApplicationId.requestForPlacementStatus)
    assertThat(aggregateSnapshot.cas1ApplicationPlacementStatus).isEqualTo(cas1ApplicationId.placementStatus)
  }
}
