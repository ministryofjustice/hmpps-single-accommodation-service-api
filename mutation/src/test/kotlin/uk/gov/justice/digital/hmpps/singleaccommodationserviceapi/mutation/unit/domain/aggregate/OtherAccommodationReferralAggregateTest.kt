package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import java.time.LocalDate
import java.util.UUID

class OtherAccommodationReferralAggregateTest {

  @Test
  fun `hydrateNew and update produces a snapshot with all fields`() {
    val caseId = UUID.randomUUID()
    val crn = "X123456"
    val localAuthorityAreaId = UUID.randomUUID()
    val submissionDate = LocalDate.of(2026, 2, 20)

    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId = caseId, crn = crn)
    aggregate.updateOtherAccommodationReferral(
      localAuthorityAreaId = localAuthorityAreaId,
      submissionDate = submissionDate,
      referenceNumber = "REF-001",
      status = OtherAccommodationReferralStatus.SUBMITTED,
      organisationName = "Organisation name",
      website = "https://www.charity.org",
      submissionNote = "A submission note",
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.id).isNotNull()
    assertThat(snapshot.caseId).isEqualTo(caseId)
    assertThat(snapshot.crn).isEqualTo(crn)
    assertThat(snapshot.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(snapshot.referenceNumber).isEqualTo("REF-001")
    assertThat(snapshot.submissionDate).isEqualTo(submissionDate)
    assertThat(snapshot.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
    assertThat(snapshot.organisationName).isEqualTo("Organisation name")
    assertThat(snapshot.website).isEqualTo("https://www.charity.org")
    assertThat(snapshot.submissionNote).isEqualTo("A submission note")
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `blank submission note is normalised to null`(note: String) {
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId = UUID.randomUUID(), crn = "X123456")
    aggregate.updateOtherAccommodationReferral(
      localAuthorityAreaId = UUID.randomUUID(),
      submissionDate = LocalDate.of(2026, 2, 20),
      referenceNumber = null,
      status = OtherAccommodationReferralStatus.SUBMITTED,
      organisationName = null,
      website = null,
      submissionNote = note,
    )

    assertThat(aggregate.snapshot().submissionNote).isNull()
  }
}
