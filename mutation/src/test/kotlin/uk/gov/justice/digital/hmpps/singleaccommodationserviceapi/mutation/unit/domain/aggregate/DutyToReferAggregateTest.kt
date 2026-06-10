package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonRequiredException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonOtherGreaterThanMaxLengthException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonRequiredException
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
  @EnumSource(value = DtrStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED", "WITHDRAWN"])
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
      outcomeReason = outcomeReasonFor(newStatus),
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
      outcomeReason = outcomeReasonFor(status),
    )

    assertThat(aggregate.snapshot().status).isEqualTo(status)
    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @Test
  fun `update should not emit domain event when status stays WITHDRAWN`() {
    val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.WITHDRAWN)

    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.DISENGAGED,
    )

    assertThat(aggregate.snapshot().status).isEqualTo(DtrStatus.WITHDRAWN)
    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `update to WITHDRAWN should emit domain event when transitioning from non-WITHDRAWN status`(currentStatus: DtrStatus) {
    val aggregate = hydrateAndCreateDutyToRefer(currentStatus)

    aggregate.updateDutyToRefer(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = null,
      status = DtrStatus.WITHDRAWN,
      withdrawalReason = WithdrawalReason.NEW_REFERRAL,
    )

    assertThat(aggregate.snapshot().status).isEqualTo(DtrStatus.WITHDRAWN)
    val domainEvents = aggregate.pullDomainEvents()
    assertThat(domainEvents).hasSize(1)
    assertThat(domainEvents.first()).isInstanceOf(DutyToReferUpdatedDomainEvent::class.java)
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

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `update should throw DutyToReferInvalidStatusTransitionException when updating from WITHDRAWN`(newStatus: DtrStatus) {
    val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.WITHDRAWN)

    assertThrows<DutyToReferInvalidStatusTransitionException> {
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = newStatus,
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

  @Nested
  inner class WithdrawalReasonValidation {
    @Test
    fun `update to WITHDRAWN without reason should throw DutyToReferWithdrawalReasonRequiredException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonRequiredException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = null,
        )
      }
    }

    @Test
    fun `update to WITHDRAWN with OTHER reason but no text should throw DutyToReferWithdrawalReasonRequiredException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonRequiredException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = WithdrawalReason.OTHER,
          withdrawalReasonOther = null,
        )
      }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
    fun `update to WITHDRAWN with OTHER reason and blank text should throw DutyToReferWithdrawalReasonRequiredException`(text: String) {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonRequiredException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = WithdrawalReason.OTHER,
          withdrawalReasonOther = text,
        )
      }
    }

    @Test
    fun `update to WITHDRAWN with OTHER reason text exceeding 4000 chars should throw DutyToReferWithdrawalReasonOtherGreaterThanMaxLengthException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonOtherGreaterThanMaxLengthException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = WithdrawalReason.OTHER,
          withdrawalReasonOther = "a".repeat(4001),
        )
      }
    }

    @Test
    fun `update to WITHDRAWN with non-OTHER reason and text should throw DutyToReferWithdrawalReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = WithdrawalReason.NEW_REFERRAL,
          withdrawalReasonOther = "should not be here",
        )
      }
    }

    @Test
    fun `update to non-WITHDRAWN status with withdrawal reason should throw DutyToReferWithdrawalReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.ACCEPTED,
          withdrawalReason = WithdrawalReason.NEW_REFERRAL,
        )
      }
    }

    @Test
    fun `update to non-WITHDRAWN status with withdrawal reason other should throw DutyToReferWithdrawalReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.ACCEPTED,
          withdrawalReasonOther = "some text",
        )
      }
    }

    @Test
    fun `update to non-WITHDRAWN status with both withdrawal fields should throw DutyToReferWithdrawalReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.ACCEPTED,
          withdrawalReason = WithdrawalReason.OTHER,
          withdrawalReasonOther = "some text",
        )
      }
    }

    @Test
    fun `update to WITHDRAWN without reason but with reason other should throw DutyToReferWithdrawalReasonRequiredException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferWithdrawalReasonRequiredException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReasonOther = "some text",
        )
      }
    }

    @Test
    fun `update to WITHDRAWN with OTHER reason and valid text should succeed`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = DtrStatus.WITHDRAWN,
        withdrawalReason = WithdrawalReason.OTHER,
        withdrawalReasonOther = "A valid withdrawal reason",
      )

      val snapshot = aggregate.snapshot()
      assertThat(snapshot.status).isEqualTo(DtrStatus.WITHDRAWN)
      assertThat(snapshot.withdrawalReason).isEqualTo(WithdrawalReason.OTHER)
      assertThat(snapshot.withdrawalReasonOther).isEqualTo("A valid withdrawal reason")
    }
  }

  @Nested
  inner class OutcomeReasonValidation {
    @ParameterizedTest
    @EnumSource(value = OutcomeReason::class, names = ["PREVENTION_AND_RELIEF_DUTY", "PRIORITY_NEED"])
    fun `update to ACCEPTED with a valid ACCEPTED outcome reason should succeed`(outcomeReason: OutcomeReason) {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = DtrStatus.ACCEPTED,
        outcomeReason = outcomeReason,
      )

      val snapshot = aggregate.snapshot()
      assertThat(snapshot.status).isEqualTo(DtrStatus.ACCEPTED)
      assertThat(snapshot.outcomeReason).isEqualTo(outcomeReason)
    }

    @ParameterizedTest
    @EnumSource(value = OutcomeReason::class, names = ["NO_LOCAL_CONNECTION", "INTENTIONALLY_HOMELESS", "REJECTED_FOR_ANOTHER_REASON"])
    fun `update to NOT_ACCEPTED with a valid NOT_ACCEPTED outcome reason should succeed`(outcomeReason: OutcomeReason) {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      aggregate.updateDutyToRefer(
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = submissionDate,
        referenceNumber = null,
        status = DtrStatus.NOT_ACCEPTED,
        outcomeReason = outcomeReason,
      )

      val snapshot = aggregate.snapshot()
      assertThat(snapshot.status).isEqualTo(DtrStatus.NOT_ACCEPTED)
      assertThat(snapshot.outcomeReason).isEqualTo(outcomeReason)
    }

    @ParameterizedTest
    @EnumSource(value = DtrStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
    fun `update to ACCEPTED or NOT_ACCEPTED without an outcome reason should throw DutyToReferOutcomeReasonRequiredException`(status: DtrStatus) {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferOutcomeReasonRequiredException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = status,
          outcomeReason = null,
        )
      }
    }

    @Test
    fun `update to ACCEPTED with a NOT_ACCEPTED outcome reason should throw DutyToReferOutcomeReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferOutcomeReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.ACCEPTED,
          outcomeReason = OutcomeReason.NO_LOCAL_CONNECTION,
        )
      }
    }

    @Test
    fun `update to NOT_ACCEPTED with an ACCEPTED outcome reason should throw DutyToReferOutcomeReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferOutcomeReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.NOT_ACCEPTED,
          outcomeReason = OutcomeReason.PRIORITY_NEED,
        )
      }
    }

    @Test
    fun `update to SUBMITTED with an outcome reason should throw DutyToReferOutcomeReasonNotApplicableException`() {
      val aggregate = DutyToReferAggregate.hydrateNew(caseId)
      assertThrows<DutyToReferOutcomeReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.SUBMITTED,
          outcomeReason = OutcomeReason.PRIORITY_NEED,
        )
      }
    }

    @Test
    fun `update to WITHDRAWN with an outcome reason should throw DutyToReferOutcomeReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateDutyToRefer(DtrStatus.SUBMITTED)
      assertThrows<DutyToReferOutcomeReasonNotApplicableException> {
        aggregate.updateDutyToRefer(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = submissionDate,
          referenceNumber = null,
          status = DtrStatus.WITHDRAWN,
          withdrawalReason = WithdrawalReason.NEW_REFERRAL,
          outcomeReason = OutcomeReason.PRIORITY_NEED,
        )
      }
    }
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
        withdrawalReason = if (status == DtrStatus.WITHDRAWN) WithdrawalReason.NEW_REFERRAL else null,
        outcomeReason = outcomeReasonFor(status),
      )
      aggregate.pullDomainEvents()
    }

    return aggregate
  }

  private fun outcomeReasonFor(status: DtrStatus): OutcomeReason? = when (status) {
    DtrStatus.ACCEPTED -> OutcomeReason.PRIORITY_NEED
    DtrStatus.NOT_ACCEPTED -> OutcomeReason.NO_LOCAL_CONNECTION
    else -> null
  }
}
