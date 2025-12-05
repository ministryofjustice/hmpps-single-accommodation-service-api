package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.api.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.toReferral
import java.time.Instant
import java.util.UUID

class ReferralMappingTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "awaitingResponse, PENDING",
      "completed,        ACCEPTED",
      "reallocated,      REJECTED",
      "inProgress,       PENDING",
      "notStarted,       PENDING",
    ]
  )
  fun `CAS1 statuses map correctly`(statusName: String, expectedStatusName: String) {
    val status = Cas1AssessmentStatus.valueOf(statusName)
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = toReferral(
      Cas1ReferralHistory(
        type = ServiceType.CAS1,
        id = id,
        applicationId = UUID.randomUUID(),
        status = status,
        createdAt = createdAt,
      ),
    )

    assertThat(referral).isEqualTo(
      Referral(
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
    ]
  )
  fun `CAS2 statuses map correctly`(status: String, expectedStatusName: String) {
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = toReferral(
      Cas2ReferralHistory(
        type = ServiceType.CAS2,
        id = id,
        applicationId = UUID.randomUUID(),
        status = status,
        createdAt = createdAt,
      ),
    )

    assertThat(referral).isEqualTo(
      Referral(
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
      toReferral(
        Cas2ReferralHistory(
          type = ServiceType.CAS2,
          id = UUID.randomUUID(),
          applicationId = UUID.randomUUID(),
          status = unknown,
          createdAt = createdAt,
        ),
      )
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
    ]
  )
  fun `CAS2v2 statuses map correctly`(status: String, expectedStatusName: String) {
    val id = UUID.randomUUID()
    val createdAt = Instant.now()

    val referral = toReferral(
      Cas2v2ReferralHistory(
        type = ServiceType.CAS2v2,
        id = id,
        applicationId = UUID.randomUUID(),
        status = status,
        createdAt = createdAt,
      ),
    )

    assertThat(referral).isEqualTo(
      Referral(
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

    val referral = toReferral(
      Cas3ReferralHistory(
        type = ServiceType.CAS3,
        id = id,
        applicationId = UUID.randomUUID(),
        status = status,
        createdAt = createdAt,
      ),
    )

    val expected = when (status) {
      TemporaryAccommodationAssessmentStatus.unallocated -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.inReview -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.readyToPlace -> CasReferralStatus.ACCEPTED
      TemporaryAccommodationAssessmentStatus.closed -> CasReferralStatus.REJECTED
      TemporaryAccommodationAssessmentStatus.rejected -> CasReferralStatus.REJECTED
    }

    assertThat(referral).isEqualTo(
      Referral(
        id = id,
        type = CasType.CAS3,
        status = expected,
        date = createdAt,
      ),
    )
  }
}
