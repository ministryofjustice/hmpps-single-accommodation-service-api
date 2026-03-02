package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrOutcomeStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import java.time.LocalDate
import java.util.UUID

class DutyToReferAggregateTest {

  @Test
  fun `should hydrateNew and updateDutyToRefer setting all fields`() {
    val crn = "ABC1234"
    val localAuthorityAreaId = UUID.randomUUID()
    val submissionDate = LocalDate.of(2026, 1, 15)
    val referenceNumber = "DTR-REF-001"

    val aggregate = DutyToReferAggregate.hydrateNew(crn)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = referenceNumber,
      outcomeStatus = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.crn).isEqualTo(crn)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.referenceNumber).isEqualTo(referenceNumber)
    assertThat(snapshot.outcomeStatus).isNull()
  }

  @Test
  fun `should hydrateNew and updateDutyToRefer with null referenceNumber`() {
    val crn = "ABC1234"
    val localAuthorityAreaId = UUID.randomUUID()
    val submissionDate = LocalDate.of(2026, 2, 20)

    val aggregate = DutyToReferAggregate.hydrateNew(crn)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      outcomeStatus = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.referenceNumber).isNull()
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
  }

  @Test
  fun `should produce domain event when outcomeStatus is YES`() {
    val aggregate = DutyToReferAggregate.hydrateNew("ABC1234")
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.now(),
      referenceNumber = null,
      outcomeStatus = DtrOutcomeStatus.YES,
    )

    val snapshot = aggregate.snapshot()
    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).hasSize(1)
    assertThat(domainEvents.first()).isInstanceOf(DutyToReferUpdatedDomainEvent::class.java)
    assertThat(domainEvents.first().aggregateId).isEqualTo(snapshot.id)
  }

  @Test
  fun `should not produce domain event when outcomeStatus is NO`() {
    val aggregate = DutyToReferAggregate.hydrateNew("ABC1234")
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.now(),
      referenceNumber = null,
      outcomeStatus = DtrOutcomeStatus.NO,
    )

    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).isEmpty()
  }

  @Test
  fun `should not produce domain event when outcomeStatus is null`() {
    val aggregate = DutyToReferAggregate.hydrateNew("ABC1234")
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.now(),
      referenceNumber = null,
      outcomeStatus = null,
    )

    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).isEmpty()
  }

  @Test
  fun `pullDomainEvents should clear events after pulling`() {
    val aggregate = DutyToReferAggregate.hydrateNew("ABC1234")
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.now(),
      referenceNumber = null,
      outcomeStatus = DtrOutcomeStatus.YES,
    )

    val firstPull = aggregate.pullDomainEvents()
    assertThat(firstPull).hasSize(1)

    val secondPull = aggregate.pullDomainEvents()
    assertThat(secondPull).isEmpty()
  }
}
