package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ExternalReferralOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ExternalReferralOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ExternalReferralOutcomeReasonRequiredException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream
import kotlin.random.Random

class ExternalReferralAggregateTest {

  private val submissionDate = LocalDate.of(2026, 1, 15)

  @Test
  fun `hydrateNew and update produces a snapshot with all fields`() {
    val caseId = UUID.randomUUID()
    val crn = "X123456"

    val aggregate = ExternalReferralAggregate.hydrateNew(caseId = caseId, crn = crn)
    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = "Organisation name",
      website = "https://www.charity.org",
      submissionNote = "A submission note",
      email = "contact@example.com",
      phoneNumber = "01234567890",
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.caseId).isEqualTo(caseId)
    assertThat(snapshot.crn).isEqualTo(crn)
    assertThat(snapshot.referenceNumber).isEqualTo("REF-001")
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
    assertThat(snapshot.organisationName).isEqualTo("Organisation name")
    assertThat(snapshot.website).isEqualTo("https://www.charity.org")
    assertThat(snapshot.submissionNote).isEqualTo("A submission note")
    assertThat(snapshot.email).isEqualTo("contact@example.com")
    assertThat(snapshot.phoneNumber).isEqualTo("01234567890")
  }

  @Test
  fun `blank contact email and contact number are treated as absent`() {
    val aggregate = ExternalReferralAggregate.hydrateNew(caseId = UUID.randomUUID(), crn = "X123456")
    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = "Organisation name",
      website = "https://www.charity.org",
      submissionNote = "A submission note",
      email = "   ",
      phoneNumber = "   ",
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.email).isNull()
    assertThat(snapshot.phoneNumber).isNull()
  }

  @Test
  fun `hydrateExisting and update produces a snapshot with the updated fields`() {
    val id = UUID.randomUUID()
    val caseId = UUID.randomUUID()
    val crn = "X123456"
    val newSubmissionDate = LocalDate.of(2026, 3, 1)

    val aggregate = ExternalReferralAggregate.hydrateExisting(
      id = id,
      caseId = caseId,
      crn = crn,
      referenceNumber = "REF-001",
      submissionDate = LocalDate.of(2026, 2, 20),
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = "Organisation name",
      website = "https://www.charity.org",
      submissionNote = "A submission note",
      notes = emptyList(),
    )

    aggregate.updateExternalReferral(
      submissionDate = newSubmissionDate,
      referenceNumber = "REF-002",
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = "New organisation name",
      website = "https://www.new-charity.org",
      submissionNote = "An updated submission note",
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isEqualTo(id)
    assertThat(snapshot.caseId).isEqualTo(caseId)
    assertThat(snapshot.crn).isEqualTo(crn)
    assertThat(snapshot.referenceNumber).isEqualTo("REF-002")
    assertThat(snapshot.submissionDate).isEqualTo(newSubmissionDate)
    assertThat(snapshot.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
    assertThat(snapshot.organisationName).isEqualTo("New organisation name")
    assertThat(snapshot.website).isEqualTo("https://www.new-charity.org")
    assertThat(snapshot.submissionNote).isEqualTo("An updated submission note")
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `blank submission note is normalised to null`(note: String) {
    val aggregate = ExternalReferralAggregate.hydrateNew(caseId = UUID.randomUUID(), crn = "X123456")
    aggregate.updateExternalReferral(
      submissionDate = LocalDate.of(2026, 2, 20),
      referenceNumber = null,
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = null,
      website = null,
      submissionNote = note,
    )

    assertThat(aggregate.snapshot().submissionNote).isNull()
  }

  @Test
  fun `should addNote successfully`() {
    val aggregate = hydrateAndCreateReferral()

    val note = "note"
    aggregate.addNote(note)

    val aggregateSnapshot = aggregate.snapshot()

    assertThat(aggregateSnapshot.notes.first().id).isNotNull
    assertThat(aggregateSnapshot.notes.first().note).isEqualTo(note)
    assertThat(aggregateSnapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(aggregateSnapshot.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `addNote should throw NoteIsEmptyException domain exception when note is blank`(note: String) {
    assertThrows<NoteIsEmptyException> {
      val aggregate = hydrateAndCreateReferral()
      aggregate.addNote(note)
    }
  }

  @Test
  fun `addNote should throw NoteIsGreaterThanMaxLengthException domain exception when note is greater than 4000 chars`() {
    assertThrows<NoteIsGreaterThanMaxLengthException> {
      val aggregate = hydrateAndCreateReferral()
      aggregate.addNote(note = "a".repeat(4001))
    }
  }

  @Test
  fun `addNote should not throw exception when the note length is within the min-length and max-length boundaries`() {
    shouldSuccessfullyAddNote(note = "a".repeat(Random.nextInt(1, 4000)))
  }

  @ParameterizedTest
  @MethodSource("validOutcomeReasonScenarios")
  fun `updateExternalReferral accepts a valid outcome reason for the given status`(
    status: ExternalReferralStatus,
    outcomeReason: ExternalReferralOutcomeReason,
  ) {
    val aggregate = hydrateAndCreateReferral()

    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = status,
      organisationName = null,
      website = null,
      submissionNote = null,
      outcomeReason = outcomeReason,
      outcomeNote = "An outcome note",
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.status).isEqualTo(status)
    assertThat(snapshot.outcomeReason).isEqualTo(outcomeReason)
    assertThat(snapshot.outcomeNote).isEqualTo("An outcome note")
  }

  @Test
  fun `updateExternalReferral throws exception when status is ACCEPTED and outcomeReason is missing`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonRequiredException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.ACCEPTED,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = null,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is ACCEPTED with a REJECTED outcome reason`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonNotApplicableException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.ACCEPTED,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = ExternalReferralOutcomeReason.REJECTED_NO_CAPACITY,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is WITHDRAWN and outcomeReason is missing`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonRequiredException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.WITHDRAWN,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = null,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is WITHDRAWN with an ACCEPTED outcome reason`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonNotApplicableException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.WITHDRAWN,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = ExternalReferralOutcomeReason.ACCEPTED,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is REJECTED with a WITHDRAWN outcome reason`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonNotApplicableException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.REJECTED,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = ExternalReferralOutcomeReason.WITHDRAWN_HOUSING_NEED_RESOLVED,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is SUBMITTED and an outcomeReason is provided`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeReasonNotApplicableException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.SUBMITTED,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeReason = ExternalReferralOutcomeReason.ACCEPTED,
      )
    }
  }

  @Test
  fun `updateExternalReferral throws exception when status is SUBMITTED and an outcomeNote is provided`() {
    val aggregate = hydrateAndCreateReferral()

    assertThrows<ExternalReferralOutcomeNoteNotApplicableException> {
      aggregate.updateExternalReferral(
        submissionDate = submissionDate,
        referenceNumber = "REF-001",
        status = ExternalReferralStatus.SUBMITTED,
        organisationName = null,
        website = null,
        submissionNote = null,
        outcomeNote = "An outcome note",
      )
    }
  }

  @Test
  fun `updateExternalReferral clears outcomeReason and outcomeNote when moving back to SUBMITTED`() {
    val aggregate = hydrateAndCreateReferral()
    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = ExternalReferralStatus.REJECTED,
      organisationName = null,
      website = null,
      submissionNote = null,
      outcomeReason = ExternalReferralOutcomeReason.REJECTED_NO_CAPACITY,
      outcomeNote = "An outcome note",
    )

    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = null,
      website = null,
      submissionNote = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.outcomeReason).isNull()
    assertThat(snapshot.outcomeNote).isNull()
  }

  private fun shouldSuccessfullyAddNote(note: String) {
    val aggregate = hydrateAndCreateReferral()
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  private fun hydrateAndCreateReferral(): ExternalReferralAggregate {
    val aggregate = ExternalReferralAggregate.hydrateNew(caseId = UUID.randomUUID(), crn = "X123456")
    aggregate.updateExternalReferral(
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = ExternalReferralStatus.SUBMITTED,
      organisationName = null,
      website = null,
      submissionNote = null,
    )
    return aggregate
  }

  companion object {
    @JvmStatic
    fun validOutcomeReasonScenarios(): Stream<Arguments> = Stream.of(
      Arguments.of(ExternalReferralStatus.ACCEPTED, ExternalReferralOutcomeReason.ACCEPTED),
      Arguments.of(ExternalReferralStatus.REJECTED, ExternalReferralOutcomeReason.REJECTED_PERSON_NOT_SUITABLE),
      Arguments.of(ExternalReferralStatus.REJECTED, ExternalReferralOutcomeReason.REJECTED_NO_CAPACITY),
      Arguments.of(ExternalReferralStatus.REJECTED, ExternalReferralOutcomeReason.REJECTED_OTHER),
      Arguments.of(ExternalReferralStatus.WITHDRAWN, ExternalReferralOutcomeReason.WITHDRAWN_APPLICANT_REJECTED_OFFER),
      Arguments.of(ExternalReferralStatus.WITHDRAWN, ExternalReferralOutcomeReason.WITHDRAWN_HOUSING_NEED_RESOLVED),
      Arguments.of(ExternalReferralStatus.WITHDRAWN, ExternalReferralOutcomeReason.WITHDRAWN_NO_RESPONSE_FROM_ORGANISATION),
    )
  }
}
