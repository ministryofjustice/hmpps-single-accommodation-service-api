package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeReasonRequiredException
import java.time.LocalDate
import java.util.UUID

class OtherAccommodationReferralAggregateTest {

  private val submissionDate = LocalDate.of(2026, 1, 15)
  private val caseId = UUID.randomUUID()

  @Test
  fun `create with SUBMITTED status should set all fields`() {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = "OOR-REF-001",
      status = OorStatus.SUBMITTED,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.caseId).isEqualTo(caseId)
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.referenceNumber).isEqualTo("OOR-REF-001")
    assertThat(snapshot.status).isEqualTo(OorStatus.SUBMITTED)
  }

  @ParameterizedTest
  @EnumSource(value = OorStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
  fun `create should throw OtherAccommodationReferralInvalidStatusException when status is not SUBMITTED`(status: OorStatus) {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)

    assertThrows<OtherAccommodationReferralInvalidStatusException> {
      aggregate.updateOtherAccommodationReferral(
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
  fun `update should change status`(currentStatus: OorStatus, newStatus: OorStatus) {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(currentStatus)

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = newStatus,
      outcomeReason = outcomeReasonFor(newStatus),
    )

    assertThat(aggregate.snapshot().status).isEqualTo(newStatus)
  }

  @ParameterizedTest
  @EnumSource(value = OorStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `update should keep status when unchanged`(status: OorStatus) {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(status)

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = status,
      outcomeReason = outcomeReasonFor(status),
    )

    assertThat(aggregate.snapshot().status).isEqualTo(status)
  }

  @ParameterizedTest
  @EnumSource(value = OorStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
  fun `update should throw OtherAccommodationReferralInvalidStatusTransitionException when reverting to SUBMITTED`(currentStatus: OorStatus) {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(currentStatus)

    assertThrows<OtherAccommodationReferralInvalidStatusTransitionException> {
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.SUBMITTED,
      )
    }
  }

  @Test
  fun `should addNote successfully`() {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    val note = "note"
    aggregate.addNote(note)

    val aggregateSnapshot = aggregate.snapshot()

    assertThat(aggregateSnapshot.notes.first().id).isNotNull
    assertThat(aggregateSnapshot.notes.first().note).isEqualTo(note)
    assertThat(aggregateSnapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(aggregateSnapshot.status).isEqualTo(OorStatus.SUBMITTED)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `addNote should throw NoteIsEmptyException domain exception when note is blank`(note: String) {
    assertThrows<NoteIsEmptyException> {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      aggregate.addNote(note)
    }
  }

  @Test
  fun `addNote should throw NoteIsGreaterThanMaxLengthException domain exception when note is greater than 4000 chars`() {
    assertThrows<NoteIsGreaterThanMaxLengthException> {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
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
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  @Nested
  inner class OutcomeReasonValidation {
    @ParameterizedTest
    @EnumSource(value = OutcomeReason::class, names = ["PREVENTION_AND_RELIEF_DUTY", "PRIORITY_NEED"])
    fun `update to ACCEPTED with a valid ACCEPTED outcome reason should succeed`(outcomeReason: OutcomeReason) {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.ACCEPTED,
        outcomeReason = outcomeReason,
      )

      val snapshot = aggregate.snapshot()
      assertThat(snapshot.status).isEqualTo(OorStatus.ACCEPTED)
      assertThat(snapshot.outcomeReason).isEqualTo(outcomeReason)
    }

    @ParameterizedTest
    @EnumSource(value = OutcomeReason::class, names = ["NO_LOCAL_CONNECTION", "INTENTIONALLY_HOMELESS", "REJECTED_FOR_ANOTHER_REASON"])
    fun `update to NOT_ACCEPTED with a valid NOT_ACCEPTED outcome reason should succeed`(outcomeReason: OutcomeReason) {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.NOT_ACCEPTED,
        outcomeReason = outcomeReason,
      )

      val snapshot = aggregate.snapshot()
      assertThat(snapshot.status).isEqualTo(OorStatus.NOT_ACCEPTED)
      assertThat(snapshot.outcomeReason).isEqualTo(outcomeReason)
    }

    @ParameterizedTest
    @EnumSource(value = OorStatus::class, names = ["ACCEPTED", "NOT_ACCEPTED"])
    fun `update to ACCEPTED or NOT_ACCEPTED without an outcome reason should throw OtherAccommodationReferralOutcomeReasonRequiredException`(status: OorStatus) {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      assertThrows<OtherAccommodationReferralOutcomeReasonRequiredException> {
        aggregate.updateOtherAccommodationReferral(
          submissionDate = submissionDate,
          referenceNumber = null,
          status = status,
          outcomeReason = null,
        )
      }
    }

    @Test
    fun `update to ACCEPTED with a NOT_ACCEPTED outcome reason should throw OtherAccommodationReferralOutcomeReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      assertThrows<OtherAccommodationReferralOutcomeReasonNotApplicableException> {
        aggregate.updateOtherAccommodationReferral(
          submissionDate = submissionDate,
          referenceNumber = null,
          status = OorStatus.ACCEPTED,
          outcomeReason = OutcomeReason.NO_LOCAL_CONNECTION,
        )
      }
    }

    @Test
    fun `update to NOT_ACCEPTED with an ACCEPTED outcome reason should throw OtherAccommodationReferralOutcomeReasonNotApplicableException`() {
      val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
      assertThrows<OtherAccommodationReferralOutcomeReasonNotApplicableException> {
        aggregate.updateOtherAccommodationReferral(
          submissionDate = submissionDate,
          referenceNumber = null,
          status = OorStatus.NOT_ACCEPTED,
          outcomeReason = OutcomeReason.PRIORITY_NEED,
        )
      }
    }

    @Test
    fun `update to SUBMITTED with an outcome reason should throw OtherAccommodationReferralOutcomeReasonNotApplicableException`() {
      val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
      assertThrows<OtherAccommodationReferralOutcomeReasonNotApplicableException> {
        aggregate.updateOtherAccommodationReferral(
          submissionDate = submissionDate,
          referenceNumber = null,
          status = OorStatus.SUBMITTED,
          outcomeReason = OutcomeReason.PRIORITY_NEED,
        )
      }
    }
  }

  private fun hydrateAndCreateOtherAccommodationReferral(status: OorStatus): OtherAccommodationReferralAggregate {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
    )

    if (status != OorStatus.SUBMITTED) {
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = status,
        outcomeReason = outcomeReasonFor(status),
      )
    }

    return aggregate
  }

  @Test
  fun `create with submission note should set submissionNote on snapshot`() {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "A submission note",
    )

    assertThat(aggregate.snapshot().submissionNote).isEqualTo("A submission note")
  }

  @Test
  fun `update with outcome note should set outcomeNote on snapshot`() {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      outcomeNote = "An outcome note",
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.outcomeNote).isEqualTo("An outcome note")
  }

  @Test
  fun `update on an outcome status should set both submissionNote and outcomeNote`() {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PREVENTION_AND_RELIEF_DUTY,
      submissionNote = "A submission note",
      outcomeNote = "An outcome note",
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.submissionNote).isEqualTo("A submission note")
    assertThat(snapshot.outcomeNote).isEqualTo("An outcome note")
  }

  @Test
  fun `updateOtherAccommodationReferral should throw NoteIsGreaterThanMaxLengthException when submissionNote exceeds 4000 chars`() {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)

    assertThrows<NoteIsGreaterThanMaxLengthException> {
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.SUBMITTED,
        submissionNote = "a".repeat(4001),
      )
    }
  }

  @Test
  fun `updateOtherAccommodationReferral should throw NoteIsGreaterThanMaxLengthException when outcomeNote exceeds 4000 chars`() {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)

    assertThrows<NoteIsGreaterThanMaxLengthException> {
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.ACCEPTED,
        outcomeReason = OutcomeReason.PRIORITY_NEED,
        outcomeNote = "a".repeat(4001),
      )
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `updateOtherAccommodationReferral should ignore blank submissionNote`(note: String) {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = note,
    )

    assertThat(aggregate.snapshot().submissionNote).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `updateOtherAccommodationReferral should ignore blank outcomeNote`(note: String) {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      outcomeNote = note,
    )

    assertThat(aggregate.snapshot().outcomeNote).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `updateOtherAccommodationReferral should clear an existing submissionNote when a blank submissionNote is provided on SUBMITTED`(note: String) {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "Original submission note",
    )
    assertThat(aggregate.snapshot().submissionNote).isEqualTo("Original submission note")

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = note,
    )

    assertThat(aggregate.snapshot().submissionNote).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `updateOtherAccommodationReferral should clear an existing outcomeNote when a blank outcomeNote is provided on an outcome status`(note: String) {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      outcomeNote = "Original outcome note",
    )
    assertThat(aggregate.snapshot().outcomeNote).isEqualTo("Original outcome note")

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      outcomeNote = note,
    )

    assertThat(aggregate.snapshot().outcomeNote).isNull()
  }

  @Test
  fun `updateOtherAccommodationReferral should replace an existing submissionNote with a new submissionNote on SUBMITTED`() {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "Original submission note",
    )

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "Updated submission note",
    )

    assertThat(aggregate.snapshot().submissionNote).isEqualTo("Updated submission note")
  }

  @Test
  fun `updateOtherAccommodationReferral should update an existing submissionNote on an outcome status`() {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "Original submission note",
    )

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      submissionNote = "Updated submission note",
    )

    assertThat(aggregate.snapshot().submissionNote).isEqualTo("Updated submission note")
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `updateOtherAccommodationReferral should clear an existing submissionNote when a blank submissionNote is provided on an outcome status`(note: String) {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.SUBMITTED,
      submissionNote = "Original submission note",
    )

    aggregate.updateOtherAccommodationReferral(
      submissionDate = submissionDate,
      referenceNumber = null,
      status = OorStatus.ACCEPTED,
      outcomeReason = OutcomeReason.PRIORITY_NEED,
      submissionNote = note,
    )

    assertThat(aggregate.snapshot().submissionNote).isNull()
  }

  @Test
  fun `updateOtherAccommodationReferral should throw OtherAccommodationReferralOutcomeNoteNotApplicableException when outcomeNote provided for SUBMITTED`() {
    val aggregate = hydrateAndCreateOtherAccommodationReferral(OorStatus.SUBMITTED)
    assertThrows<OtherAccommodationReferralOutcomeNoteNotApplicableException> {
      aggregate.updateOtherAccommodationReferral(
        submissionDate = submissionDate,
        referenceNumber = null,
        status = OorStatus.SUBMITTED,
        outcomeNote = "should not be here",
      )
    }
  }

  private fun outcomeReasonFor(status: OorStatus): OutcomeReason? = when (status) {
    OorStatus.ACCEPTED -> OutcomeReason.PRIORITY_NEED
    OorStatus.NOT_ACCEPTED -> OutcomeReason.NO_LOCAL_CONNECTION
    OorStatus.SUBMITTED -> null
  }
}
