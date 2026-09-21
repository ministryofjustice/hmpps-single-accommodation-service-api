package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1SpaceBookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralStatusMapper

class AccommodationReferralStatusMapperTest {

  @ParameterizedTest
  @EnumSource(Cas1SpaceBookingStatus::class)
  fun `should transform CAS1 space booking status`(status: Cas1SpaceBookingStatus) {
    val expected = when (status) {
      Cas1SpaceBookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
      Cas1SpaceBookingStatus.NOT_ARRIVED -> AccommodationReferralStatus.NOT_ARRIVED
      Cas1SpaceBookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
      Cas1SpaceBookingStatus.ARRIVED,
      Cas1SpaceBookingStatus.UPCOMING,
      -> AccommodationReferralStatus.ACCEPTED
    }

    val referral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.STARTED,
      placementStatus = status,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(RequestForPlacementStatus::class)
  fun `should transform CAS1 request for placement status`(status: RequestForPlacementStatus) {
    val expected = when (status) {
      RequestForPlacementStatus.REQUEST_REJECTED -> AccommodationReferralStatus.REQUEST_REJECTED
      RequestForPlacementStatus.REQUEST_WITHDRAWN -> AccommodationReferralStatus.REQUEST_WITHDRAWN
      RequestForPlacementStatus.PLACEMENT_BOOKED -> AccommodationReferralStatus.ACCEPTED
      RequestForPlacementStatus.REQUEST_UNSUBMITTED,
      RequestForPlacementStatus.REQUEST_SUBMITTED,
      RequestForPlacementStatus.AWAITING_MATCH,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.STARTED,
      requestForPlacementStatus = status,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApprovedPremisesApplicationStatus::class)
  fun `should transform CAS1 application status`(status: ApprovedPremisesApplicationStatus) {
    val expected = when (status) {
      ApprovedPremisesApplicationStatus.EXPIRED -> AccommodationReferralStatus.EXPIRED
      ApprovedPremisesApplicationStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
      ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED -> AccommodationReferralStatus.ACCEPTED
      ApprovedPremisesApplicationStatus.REJECTED,
      ApprovedPremisesApplicationStatus.INAPPLICABLE,
      -> AccommodationReferralStatus.REJECTED
      ApprovedPremisesApplicationStatus.STARTED,
      ApprovedPremisesApplicationStatus.AWAITING_ASSESSMENT,
      ApprovedPremisesApplicationStatus.UNALLOCATED_ASSESSMENT,
      ApprovedPremisesApplicationStatus.ASSESSMENT_IN_PROGRESS,
      ApprovedPremisesApplicationStatus.AWAITING_PLACEMENT,
      ApprovedPremisesApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      ApprovedPremisesApplicationStatus.PENDING_PLACEMENT_REQUEST,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = status,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @Test
  fun `should respect precedence of statuses for CAS1`() {
    val bookingReferral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.EXPIRED,
      requestForPlacementStatus = RequestForPlacementStatus.PLACEMENT_BOOKED,
      placementStatus = Cas1SpaceBookingStatus.CANCELLED,
      referredBy = buildDeliusUserDto(),
    )
    assertThat(AccommodationReferralStatusMapper.toStatus(bookingReferral)).isEqualTo(AccommodationReferralStatus.CANCELLED)

    val placementRequestReferral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.EXPIRED,
      requestForPlacementStatus = RequestForPlacementStatus.REQUEST_REJECTED,
      placementStatus = null,
      referredBy = buildDeliusUserDto(),
    )
    assertThat(AccommodationReferralStatusMapper.toStatus(placementRequestReferral)).isEqualTo(AccommodationReferralStatus.REQUEST_REJECTED)

    val applicationStatusReferral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.EXPIRED,
      requestForPlacementStatus = RequestForPlacementStatus.AWAITING_MATCH,
      placementStatus = null,
      referredBy = buildDeliusUserDto(),
    )
    assertThat(AccommodationReferralStatusMapper.toStatus(applicationStatusReferral)).isEqualTo(AccommodationReferralStatus.EXPIRED)
  }

  @ParameterizedTest
  @EnumSource(Cas3BookingStatus::class)
  fun `should transform CAS3 booking status`(status: Cas3BookingStatus) {
    val expected = when (status) {
      Cas3BookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
      Cas3BookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
      Cas3BookingStatus.NOT_MINUS_ARRIVED,
      Cas3BookingStatus.ARRIVED,
      Cas3BookingStatus.CONFIRMED,
      -> AccommodationReferralStatus.ACCEPTED
      Cas3BookingStatus.PROVISIONAL,
      Cas3BookingStatus.CLOSED,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = ApplicationStatus.SUBMITTED,
      assessmentStatus = AssessmentStatus.READY_TO_PLACE,
      bookingStatus = status,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(AssessmentStatus::class)
  fun `should transform CAS3 assessment status`(status: AssessmentStatus) {
    val expected = when (status) {
      AssessmentStatus.REJECTED -> AccommodationReferralStatus.REJECTED
      AssessmentStatus.UNALLOCATED,
      AssessmentStatus.IN_REVIEW,
      AssessmentStatus.READY_TO_PLACE,
      -> AccommodationReferralStatus.PENDING
      AssessmentStatus.CLOSED -> AccommodationReferralStatus.ARCHIVED
    }

    val referral = buildReferralHistory(
      applicationStatus = ApplicationStatus.SUBMITTED,
      assessmentStatus = status,
      referralRejectionReason = "reason",
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(AssessmentStatus::class)
  fun `should transform CAS3 assessment status without reason`(status: AssessmentStatus) {
    val expected = when (status) {
      AssessmentStatus.REJECTED,
      AssessmentStatus.CLOSED,
      -> AccommodationReferralStatus.ARCHIVED
      AssessmentStatus.UNALLOCATED,
      AssessmentStatus.IN_REVIEW,
      AssessmentStatus.READY_TO_PLACE,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = ApplicationStatus.SUBMITTED,
      assessmentStatus = status,
      referralRejectionReason = null,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApplicationStatus::class)
  fun `should transform CAS3 application status`(status: ApplicationStatus) {
    val expected = when (status) {
      ApplicationStatus.REJECTED -> AccommodationReferralStatus.REJECTED
      ApplicationStatus.IN_PROGRESS,
      ApplicationStatus.SUBMITTED,
      ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = status,
      assessmentStatus = AssessmentStatus.READY_TO_PLACE,
      referralRejectionReason = "reason",
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApplicationStatus::class)
  fun `should transform CAS3 application status without reason`(status: ApplicationStatus) {
    val expected = when (status) {
      ApplicationStatus.REJECTED -> AccommodationReferralStatus.ARCHIVED
      ApplicationStatus.IN_PROGRESS,
      ApplicationStatus.SUBMITTED,
      ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      -> AccommodationReferralStatus.PENDING
    }

    val referral = buildReferralHistory(
      applicationStatus = status,
      assessmentStatus = AssessmentStatus.READY_TO_PLACE,
      referralRejectionReason = null,
      referredBy = buildDeliusUserDto(),
    )

    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(DtrStatus::class)
  fun `should transform DTR status`(status: DtrStatus) {
    val expected = when (status) {
      DtrStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
      DtrStatus.ACCEPTED -> AccommodationReferralStatus.ACCEPTED
      DtrStatus.NOT_ACCEPTED -> AccommodationReferralStatus.REJECTED
      DtrStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
    }

    val dtr = buildDutyToReferDto(status = status)

    assertThat(AccommodationReferralStatusMapper.toStatus(dtr)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(
    "moreInfoRequested, MORE_INFORMATION_REQUESTED",
    "placeOffered, PLACE_OFFERED",
    "awaitingArrival, AWAITING_ARRIVAL",
    "cancelled, CANCELLED",
    "withdrawn, WITHDRAWN",
    "awaitingDecision, AWAITING_DECISION",
    "onWaitingList, ON_WAITING_LIST",
    "offerAccepted, ACCEPTED",
    "offerDeclined, OFFER_DECLINED_OR_WITHDRAWN",
    "unknown, PENDING",
    ", PENDING",
  )
  fun `should transform CAS2 application status`(status: String?, expected: AccommodationReferralStatus) {
    val referral = buildReferralHistory(
      applicationStatus = status ?: "",
      referredBy = buildDeliusUserDto(),
    )
    assertThat(AccommodationReferralStatusMapper.toStatus(referral)).isEqualTo(expected)
  }
}
