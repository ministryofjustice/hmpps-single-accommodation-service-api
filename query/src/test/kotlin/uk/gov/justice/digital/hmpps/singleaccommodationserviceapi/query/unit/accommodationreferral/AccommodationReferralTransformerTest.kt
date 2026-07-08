package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildStaffDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1SpaceBookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationReferralOrchestrationDto
import java.util.stream.Stream

class AccommodationReferralTransformerTest {
  @Test
  fun `should transform orchestration dto to list of accommodation referral dtos`() {
    val orchestrationDto = buildAccommodationReferralOrchestrationDto()

    val result = AccommodationReferralTransformer.transformReferrals(
      orchestrationDto,
      listOf(buildDutyToReferDto(submission = buildDtrSubmission(createdByUsername = "TEST_USER"))),
    )

    assertThat(result).hasSize(3)
    assertThat(result.map { it.type }).containsExactlyInAnyOrder(
      AccommodationService.CAS1,
      AccommodationService.CAS3,
      AccommodationService.DTR,
    )

    result.forEach {
      when (it.type) {
        AccommodationService.DTR -> {
          assertThat(it.referralRejectionReason).isEqualTo("NOT_ELIGIBLE")
          assertThat(it.localAuthorityArea).isEqualTo("localAuthorityAreaName")
          assertThat(it.pdu).isEqualTo("localAuthorityAreaName")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto("Someone", "TEST_USER", null))
          assertThat(it.placementAddress).isNull()
          assertThat(it.placementStatus).isEqualTo("NO_LOCAL_CONNECTION")
          assertThat(it.uiUrl).isNull()
        }
        AccommodationService.CAS1 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("notArrived")
        }
        AccommodationService.CAS3 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("departed")
          assertThat(it.uiUrl).isEqualTo("https://example.com/referral")
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("cas1StatusMappings")
  fun `should transform CAS1 statuses to CasReferralStatus`(
    placementStatus: Cas1SpaceBookingStatus?,
    requestForPlacementStatus: RequestForPlacementStatus?,
    applicationStatus: ApprovedPremisesApplicationStatus,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(placementStatus, requestForPlacementStatus, applicationStatus)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas3StatusMappings")
  fun `should transform CAS3 statuses to CasReferralStatus`(
    bookingStatus: Cas3BookingStatus?,
    assessmentStatus: AssessmentStatus?,
    applicationStatus: ApplicationStatus,
    referralRejectionReason: String?,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(bookingStatus, assessmentStatus, applicationStatus, referralRejectionReason)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("dtrStatusMappings")
  fun `should transform DTR statuses to CasReferralStatus`(input: DtrStatus, expected: AccommodationReferralStatus) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  private companion object {
    @JvmStatic
    fun cas1StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas1SpaceBookingStatus.CANCELLED, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.CANCELLED),
      Arguments.of(Cas1SpaceBookingStatus.NOT_ARRIVED, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.NOT_ARRIVED),
      Arguments.of(Cas1SpaceBookingStatus.DEPARTED, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.DEPARTED),
      Arguments.of(Cas1SpaceBookingStatus.ARRIVED, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas1SpaceBookingStatus.UPCOMING, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.ACCEPTED),

      Arguments.of(null, RequestForPlacementStatus.REQUEST_REJECTED, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.REQUEST_REJECTED),
      Arguments.of(null, RequestForPlacementStatus.REQUEST_WITHDRAWN, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.REQUEST_WITHDRAWN),
      Arguments.of(null, RequestForPlacementStatus.PLACEMENT_BOOKED, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.ACCEPTED),

      Arguments.of(null, null, ApprovedPremisesApplicationStatus.EXPIRED, AccommodationReferralStatus.EXPIRED),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.WITHDRAWN, AccommodationReferralStatus.WITHDRAWN),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.REJECTED, AccommodationReferralStatus.REJECTED),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.INAPPLICABLE, AccommodationReferralStatus.REJECTED),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.AWAITING_ASSESSMENT, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.UNALLOCATED_ASSESSMENT, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.ASSESSMENT_IN_PROGRESS, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.AWAITING_PLACEMENT, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.REQUESTED_FURTHER_INFORMATION, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.PENDING_PLACEMENT_REQUEST, AccommodationReferralStatus.PENDING),
      Arguments.of(null, null, ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED, AccommodationReferralStatus.ACCEPTED),

      Arguments.of(null, RequestForPlacementStatus.AWAITING_MATCH, ApprovedPremisesApplicationStatus.EXPIRED, AccommodationReferralStatus.EXPIRED),
    )

    @JvmStatic
    fun cas3StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas3BookingStatus.DEPARTED, null, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.DEPARTED),
      Arguments.of(Cas3BookingStatus.CANCELLED, null, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.CANCELLED),
      Arguments.of(Cas3BookingStatus.NOT_MINUS_ARRIVED, null, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas3BookingStatus.ARRIVED, null, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas3BookingStatus.CONFIRMED, null, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.ACCEPTED),

      Arguments.of(Cas3BookingStatus.PROVISIONAL, AssessmentStatus.READY_TO_PLACE, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas3BookingStatus.CLOSED, AssessmentStatus.READY_TO_PLACE, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.PENDING),

      Arguments.of(null, AssessmentStatus.REJECTED, ApplicationStatus.SUBMITTED, "Reason", AccommodationReferralStatus.REJECTED),
      Arguments.of(null, AssessmentStatus.REJECTED, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.ARCHIVED),
      Arguments.of(null, AssessmentStatus.READY_TO_PLACE, ApplicationStatus.REJECTED, "Reason", AccommodationReferralStatus.REJECTED),
      Arguments.of(null, AssessmentStatus.READY_TO_PLACE, ApplicationStatus.REJECTED, null, AccommodationReferralStatus.ARCHIVED),

      Arguments.of(null, AssessmentStatus.UNALLOCATED, ApplicationStatus.SUBMITTED, null, AccommodationReferralStatus.PENDING),
      Arguments.of(null, AssessmentStatus.IN_REVIEW, ApplicationStatus.IN_PROGRESS, null, AccommodationReferralStatus.PENDING),
    )

    @JvmStatic
    fun dtrStatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(DtrStatus.SUBMITTED, AccommodationReferralStatus.PENDING),
      Arguments.of(DtrStatus.ACCEPTED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(DtrStatus.NOT_ACCEPTED, AccommodationReferralStatus.REJECTED),
      Arguments.of(DtrStatus.WITHDRAWN, AccommodationReferralStatus.WITHDRAWN),
    )
  }
}
