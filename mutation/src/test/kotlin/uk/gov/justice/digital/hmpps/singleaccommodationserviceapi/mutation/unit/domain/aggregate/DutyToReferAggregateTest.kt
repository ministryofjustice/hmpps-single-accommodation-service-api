package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferCreatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import java.time.LocalDate
import java.util.UUID

class DutyToReferAggregateTest {

  @Test
  fun `should hydrateNew and createDutyToRefer setting all fields and producing domain event`() {
    val crn = "ABC1234"
    val localAuthorityAreaId = UUID.randomUUID()
    val submissionDate = LocalDate.of(2026, 1, 15)
    val referenceNumber = "DTR-REF-001"

    val aggregate = DutyToReferAggregate.hydrateNew(crn)
    aggregate.createDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = referenceNumber,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.crn).isEqualTo(crn)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.referenceNumber).isEqualTo(referenceNumber)
    assertThat(snapshot.outcomeStatus).isNull()
    assertThat(snapshot.outcomeDate).isNull()

    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).hasSize(1)
    assertThat(domainEvents.first()).isInstanceOf(DutyToReferCreatedDomainEvent::class.java)
    assertThat(domainEvents.first().aggregateId).isEqualTo(snapshot.id)
  }

  @Test
  fun `should hydrateNew and createDutyToRefer with null referenceNumber`() {
    val crn = "ABC1234"
    val localAuthorityAreaId = UUID.randomUUID()
    val submissionDate = LocalDate.of(2026, 2, 20)

    val aggregate = DutyToReferAggregate.hydrateNew(crn)
    aggregate.createDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.referenceNumber).isNull()
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
  }

  @Test
  fun `pullDomainEvents should clear events after pulling`() {
    val aggregate = DutyToReferAggregate.hydrateNew("ABC1234")
    aggregate.createDutyToRefer(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.now(),
      referenceNumber = null,
    )

    val firstPull = aggregate.pullDomainEvents()
    assertThat(firstPull).hasSize(1)

    val secondPull = aggregate.pullDomainEvents()
    assertThat(secondPull).isEmpty()
  }
}
