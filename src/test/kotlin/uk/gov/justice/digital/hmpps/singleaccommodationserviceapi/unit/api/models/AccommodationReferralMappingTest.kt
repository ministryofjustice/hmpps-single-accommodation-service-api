package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.api.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral.CasType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas3ReferralHistory
import java.time.Instant
import java.util.UUID

class AccommodationReferralMappingTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "awaiting_response, PENDING",
      "completed,         ACCEPTED",
      "reallocated,       REJECTED",
      "in_progress,       PENDING",
      "not_started,       PENDING",
    ],
  )
  fun `CAS1 statuses map correctly`(statusName: String, expectedStatusName: String) {
    val status = Cas1AssessmentStatus.forValue(statusName)
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = AccommodationReferralDto(buildCas1ReferralHistory(id, status, createdAt).first())

    assertThat(referral).isEqualTo(
      AccommodationReferralDto(
        id = id,
        type = CasType.CAS1,
        status = CasReferralStatus.valueOf(expectedStatusName),
        date = createdAt,
      ),
    )
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "More information requested,  PENDING",
      "Place offered,               ACCEPTED",
      "Awaiting arrival,            PENDING",
      "Referral cancelled,          REJECTED",
      "Referral withdrawn,          REJECTED",
      "Offer accepted,              ACCEPTED",
      "On waiting list,             PENDING",
      "Awaiting decision,           PENDING",
      "Offer declined or withdrawn, REJECTED",
    ],
  )
  fun `CAS2 statuses map correctly`(status: String, expectedStatusName: String) {
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = AccommodationReferralDto(buildCas2ReferralHistory(id, status, createdAt).first())

    assertThat(referral).isEqualTo(
      AccommodationReferralDto(
        id = id,
        type = CasType.CAS2,
        status = CasReferralStatus.valueOf(expectedStatusName),
        date = createdAt,
      ),
    )
  }

  @Test
  fun `CAS2 unknown status throws`() {
    val createdAt = Instant.now()
    val unknown = "Something else"

    assertThatThrownBy {
      AccommodationReferralDto(buildCas2ReferralHistory(UUID.randomUUID(), unknown, createdAt).first())
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unknown CAS2 referral status: $unknown")
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "More information requested,  PENDING",
      "Place offered,               ACCEPTED",
      "Awaiting arrival,            PENDING",
      "Referral cancelled,          REJECTED",
      "Referral withdrawn,          REJECTED",
      "Offer accepted,              ACCEPTED",
      "On waiting list,             PENDING",
      "Awaiting decision,           PENDING",
      "Offer declined or withdrawn, REJECTED",
    ],
  )
  fun `CAS2v2 statuses map correctly`(status: String, expectedStatusName: String) {
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = AccommodationReferralDto(buildCas2v2ReferralHistory(id, status, createdAt).first())

    assertThat(referral).isEqualTo(
      AccommodationReferralDto(
        id = id,
        type = CasType.CAS2v2,
        status = CasReferralStatus.valueOf(expectedStatusName),
        date = createdAt,
      ),
    )
  }

  @ParameterizedTest
  @EnumSource(TemporaryAccommodationAssessmentStatus::class)
  fun `CAS3 statuses map correctly`(status: TemporaryAccommodationAssessmentStatus) {
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = AccommodationReferralDto(buildCas3ReferralHistory(id, status, createdAt).first())

    val expected = when (status) {
      TemporaryAccommodationAssessmentStatus.UNALLOCATED -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.IN_REVIEW -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> CasReferralStatus.ACCEPTED
      TemporaryAccommodationAssessmentStatus.CLOSED -> CasReferralStatus.REJECTED
      TemporaryAccommodationAssessmentStatus.REJECTED -> CasReferralStatus.REJECTED
    }

    assertThat(referral).isEqualTo(
      AccommodationReferralDto(
        id = id,
        type = CasType.CAS3,
        status = expected,
        date = createdAt,
      ),
    )
  }
}
