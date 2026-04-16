package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

class DutyToReferAggregateTest {

  private val localAuthorityAreaId = UUID.randomUUID()
  private val submissionDate = LocalDate.of(2026, 1, 15)
  private val caseId = UUID.randomUUID()

  @Test
  fun `create with SUBMITTED status should set all fields and emit domain event`() {
    val aggregate = DutyToReferAggregate.hydrateNew(caseId)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = "DTR-REF-001",
      status = DtrStatus.SUBMITTED,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.caseId).isEqualTo(caseId)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.referenceNumber).isEqualTo("DTR-REF-001")
    assertThat(snapshot.status).isEqualTo(DtrStatus.SUBMITTED)

    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).hasSize(1)
    assertThat(domainEvents.first()).isInstanceOf(DutyToReferUpdatedDomainEvent::class.java)
    assertThat(domainEvents.first().aggregateId).isEqualTo(snapshot.id)
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
  fun `create should throw DutyToReferInvalidStatusException when status is not SUBMITTED`(status: DtrStatus) {
    val aggregate = DutyToReferAggregate.hydrateNew(caseId)

    assertThrows<DutyToReferInvalidStatusException> {
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = status,
      )
    }
  }

  @ParameterizedTest
  @CsvSource(
    "SUBMITTED, ACCEPTED",
    "SUBMITTED, NOT_ACCEPTED",
    "ACCEPTED, NOT_ACCEPTED",
    "NOT_ACCEPTED, ACCEPTED",
  )
  fun `update should emit domain event when status changes`(currentStatus: DtrStatus, newStatus: DtrStatus) {
    val aggregate = hydrateAndCreateDutyToRefer(currentStatus)

    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = newStatus,
    )

    assertThat(aggregate.snapshot().status).isEqualTo(newStatus)
    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).hasSize(1)
    assertThat(domainEvents.first()).isInstanceOf(DutyToReferUpdatedDomainEvent::class.java)
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `update should not emit domain event when status stays the same`(status: DtrStatus) {
    val aggregate = hydrateAndCreateDutyToRefer(status)

    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = status,
    )

    assertThat(aggregate.snapshot().status).isEqualTo(status)
    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
  fun `update should throw DutyToReferInvalidStatusTransitionException when reverting to SUBMITTED`(currentStatus: DtrStatus) {
    val aggregate = hydrateAndCreateDutyToRefer(currentStatus)

    assertThrows<DutyToReferInvalidStatusTransitionException> {
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = DtrStatus.SUBMITTED,
      )
    }
  }

  @Test
  fun `pullDomainEvents should clear events after pulling`() {
    val aggregate = DutyToReferAggregate.hydrateNew(caseId)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = DtrStatus.SUBMITTED,
    )

    val firstPull = aggregate.pullDomainEvents()
    assertThat(firstPull).hasSize(1)

    val secondPull = aggregate.pullDomainEvents()
    assertThat(secondPull).isEmpty()
  }

  @Test
  fun `should addNote successfully`() {
    val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
    val note = "note"
    aggregate.addNote(note)

    val aggregateSnapshot = aggregate.snapshot()

    assertThat(aggregateSnapshot.notes.first().id).isNotNull
    assertThat(aggregateSnapshot.notes.first().note).isEqualTo(note)
    assertThat(aggregateSnapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(aggregateSnapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(aggregateSnapshot.status).isEqualTo(DtrStatus.SUBMITTED)
    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `addNote should throw NoteIsEmptyException domain exception when note is blank`(note: String) {
    assertThrows<NoteIsEmptyException> {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      aggregate.addNote(note)
    }
  }

  @Test
  fun `addNote should throw NoteIsGreaterThanMaxLengthException domain exception when note is greater than 4000 chars`() {
    assertThrows<NoteIsGreaterThanMaxLengthException> {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      aggregate.addNote(note = "a".repeat(4001))
    }
  }

  @Test
  fun `addNote should not throw exception when the note length is within the min-length and max-length boundaries`() {
    shouldSuccessfullyAddNote(note = "a")
    shouldSuccessfullyAddNote(note = "a".repeat(10))
    shouldSuccessfullyAddNote(note = "a".repeat(100))
    shouldSuccessfullyAddNote(note = "a".repeat(1000))
    shouldSuccessfullyAddNote(note = "a".repeat(2000))
    shouldSuccessfullyAddNote(note = "a".repeat(3000))
    shouldSuccessfullyAddNote(note = "a".repeat(4000))
  }

  private fun shouldSuccessfullyAddNote(note: String) {
    val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  private fun hydrateAndCreateDutyToRefer(status: DtrStatus): DutyToReferAggregate {
    val aggregate = DutyToReferAggregate.hydrateNew(caseId)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = DtrStatus.SUBMITTED,
    )
    aggregate.pullDomainEvents()

    if (status != DtrStatus.SUBMITTED) {
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = status,
      )
      aggregate.pullDomainEvents()
    }

    return aggregate
  }
}
