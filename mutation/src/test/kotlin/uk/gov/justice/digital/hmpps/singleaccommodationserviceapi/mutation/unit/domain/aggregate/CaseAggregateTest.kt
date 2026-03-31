package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseMutationOrchestrationDto
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
    val cas1ApplicationId = UUID.randomUUID()
    val cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    val cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED
    val cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING

    val hydrated = CaseAggregate.hydrate(
      id = id,
      tierScore = tierScore,
      caseIdentifiers = caseIdentifiers,
      cas1ApplicationId = cas1ApplicationId,
      cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
    )

    assertThat(hydrated.snapshot()).satisfies(
      {
        assertThat(it.id).isEqualTo(id)
        assertThat(it.tierScore).isEqualTo(tierScore)
        assertThat(it.caseIdentifiers).isEqualTo(caseIdentifiers)
        assertThat(it.cas1ApplicationId).isEqualTo(cas1ApplicationId)
        assertThat(it.cas1ApplicationApplicationStatus).isEqualTo(cas1ApplicationApplicationStatus)
        assertThat(it.cas1ApplicationRequestForPlacementStatus).isEqualTo(cas1ApplicationRequestForPlacementStatus)
        assertThat(it.cas1ApplicationPlacementStatus).isEqualTo(cas1ApplicationPlacementStatus)
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
        tierScore = null,
        cas1ApplicationId = null,
        cas1ApplicationApplicationStatus = null,
        cas1ApplicationRequestForPlacementStatus = null,
        cas1ApplicationPlacementStatus = null,
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
    assertThat(beforeUpdate.tierScore).isNull()

    aggregate.updateTier(TierScore.A1)
    val afterUpdate = aggregate.snapshot()
    assertThat(afterUpdate.tierScore).isEqualTo(TierScore.A1)
  }

  @Test
  fun `should upsertCase`() {
    val id = UUID.randomUUID()
    val crn = "ABC1234"
    val tierScore = TierScore.A1
    val aggregate = CaseAggregate.createNew(
      id = id,
      caseIdentifiers = caseIdentifiers,
    )
    val cas1ApplicationId = buildCas1Application()
    val case = buildCaseMutationOrchestrationDto(
      crn = crn,
      cpr = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn))),
      tier = buildTier(tierScore),
      cas1Application = cas1ApplicationId,
    )

    aggregate.upsertCase(
      tierScore = case.tier?.tierScore,
      cas1ApplicationId = case.cas1Application?.id,
      cas1ApplicationApplicationStatus = case.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = case.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = case.cas1Application?.placementStatus,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.tierScore).isEqualTo(tierScore)
    assertThat(aggregateSnapshot.cas1ApplicationId).isEqualTo(cas1ApplicationId.id)
    assertThat(aggregateSnapshot.cas1ApplicationApplicationStatus).isEqualTo(cas1ApplicationId.applicationStatus)
    assertThat(aggregateSnapshot.cas1ApplicationRequestForPlacementStatus).isEqualTo(cas1ApplicationId.requestForPlacementStatus)
    assertThat(aggregateSnapshot.cas1ApplicationPlacementStatus).isEqualTo(cas1ApplicationId.placementStatus)
  }
}
