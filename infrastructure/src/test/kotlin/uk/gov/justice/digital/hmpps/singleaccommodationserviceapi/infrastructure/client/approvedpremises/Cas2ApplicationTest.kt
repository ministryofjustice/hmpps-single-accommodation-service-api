package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant
import java.time.OffsetDateTime

class Cas2ApplicationTest {

  @Nested
  inner class Cas2SubmittedApplicationSummaryTest {
    @ParameterizedTest
    @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["CANCELLED", "OFFER_DECLINED"])
    fun `A non-cancellation or non-offer-declined status never has any reasons - valid`(
      status: Cas2AssessmentStatus,
    ) {
      val thrown = catchThrowable {
        Cas2SubmittedApplicationSummary(
          latestAssessmentStatus = status,
          offerDeclinedReason = null,
          cancelledReason = null,
          submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
          markedAsArrivedDateTime = null,
        )
      }
      assertThat(thrown).isNull()
    }

    @Nested
    inner class CancellationReason {
      @ParameterizedTest
      @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["CANCELLED"])
      fun `Only a cancellation status requires a cancellation reason - invalid`(
        status: Cas2AssessmentStatus,
      ) {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = null,
            cancelledReason = "a reason",
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).hasMessage("Cancelled reason is required when status is `cancelled` and forbidden otherwise")
      }

      @Test
      fun `A cancellation status requires a cancellation reason - valid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.CANCELLED,
            offerDeclinedReason = null,
            cancelledReason = "a reason",
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).isNull()
      }

      @Test
      fun `A CANCELLED status does not have a cancellation reason - invalid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.CANCELLED,
            offerDeclinedReason = null,
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).hasMessage("Cancelled reason is required when status is `cancelled` and forbidden otherwise")
      }
    }

    @Nested
    inner class OfferDeclinedReason {
      @ParameterizedTest
      @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["OFFER_DECLINED", "CANCELLED"])
      fun `Only an offer declined status requires an offer declined reason - invalid`(
        status: Cas2AssessmentStatus,
      ) {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = "a reason",
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).hasMessage("Offer declined reason is required when status is `offerDeclined` and forbidden otherwise")
      }

      // this includes the CANCELLED status, which is handled separately from the other statuses as it will fail on the cancellation reason first
      @Test
      fun `Only an offer declined status requires an offer declined reason - invalid - CANCELLED`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.CANCELLED,
            offerDeclinedReason = "a reason",
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).hasMessage("Cancelled reason is required when status is `cancelled` and forbidden otherwise")
      }

      @Test
      fun `An offer declined status requires an offer declined reason - valid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.OFFER_DECLINED,
            offerDeclinedReason = "a reason",
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).isNull()
      }

      @Test
      fun `An OFFER_DECLINED status does not have an offer declined reason - invalid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.OFFER_DECLINED,
            offerDeclinedReason = null,
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).hasMessage("Offer declined reason is required when status is `offerDeclined` and forbidden otherwise")
      }
    }

    @Nested
    inner class MarkedAsArrivedDateTime {
      @ParameterizedTest
      @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["AWAITING_ARRIVAL", "OFFER_DECLINED", "CANCELLED"])
      fun `Only an awaiting arrival status can include a marked as arrived timestamp - invalid`(
        status: Cas2AssessmentStatus,
      ) {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = null,
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = Instant.parse("2023-02-01T00:00:00.000Z"),
          )
        }
        assertThat(thrown).hasMessage("Marked as arrived date time can only be provided when status is `awaitingArrival`")
      }

      @ParameterizedTest
      @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.INCLUDE, names = ["OFFER_DECLINED"])
      fun `An offer declined status cannot include a marked as arrived timestamp - invalid`(
        status: Cas2AssessmentStatus,
      ) {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = "a reason",
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = Instant.parse("2023-02-01T00:00:00.000Z"),
          )
        }
        assertThat(thrown).hasMessage("Marked as arrived date time can only be provided when status is `awaitingArrival`")
      }

      @ParameterizedTest
      @EnumSource(Cas2AssessmentStatus::class, mode = EnumSource.Mode.INCLUDE, names = ["CANCELLED"])
      fun `A cancelled status cannot include a marked as arrived timestamp - invalid`(
        status: Cas2AssessmentStatus,
      ) {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = null,
            cancelledReason = "a reason",
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = Instant.parse("2023-02-01T00:00:00.000Z"),
          )
        }
        assertThat(thrown).hasMessage("Marked as arrived date time can only be provided when status is `awaitingArrival`")
      }

      @Test
      fun `An awaiting arrival status can include a marked as arrived timestamp - valid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
            offerDeclinedReason = null,
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = Instant.parse("2023-02-01T00:00:00.000Z"),
          )
        }
        assertThat(thrown).isNull()
      }

      @Test
      fun `An awaiting arrival status can include a null marked as arrived timestamp - valid`() {
        val thrown = catchThrowable {
          Cas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
            offerDeclinedReason = null,
            cancelledReason = null,
            submittedAt = OffsetDateTime.parse("2023-02-01T00:00:00.000Z"),
            markedAsArrivedDateTime = null,
          )
        }
        assertThat(thrown).isNull()
      }
    }
  }
}
