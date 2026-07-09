package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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
  @EnumSource(Cas1SpaceBookingStatus::class)
  fun `should transform CAS1 space booking status`(status: Cas1SpaceBookingStatus) {
    val expected = when (status) {
      Cas1SpaceBookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
      Cas1SpaceBookingStatus.NOT_ARRIVED -> AccommodationReferralStatus.NOT_ARRIVED
      Cas1SpaceBookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
      Cas1SpaceBookingStatus.ARRIVED -> AccommodationReferralStatus.ACCEPTED
      Cas1SpaceBookingStatus.UPCOMING -> AccommodationReferralStatus.ACCEPTED
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(status, null, ApprovedPremisesApplicationStatus.STARTED)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(RequestForPlacementStatus::class)
  fun `should transform CAS1 request for placement status`(status: RequestForPlacementStatus) {
    val expected = when (status) {
      RequestForPlacementStatus.REQUEST_REJECTED -> AccommodationReferralStatus.REQUEST_REJECTED
      RequestForPlacementStatus.REQUEST_WITHDRAWN -> AccommodationReferralStatus.REQUEST_WITHDRAWN
      RequestForPlacementStatus.PLACEMENT_BOOKED -> AccommodationReferralStatus.ACCEPTED
      RequestForPlacementStatus.REQUEST_UNSUBMITTED -> AccommodationReferralStatus.PENDING
      RequestForPlacementStatus.REQUEST_SUBMITTED -> AccommodationReferralStatus.PENDING
      RequestForPlacementStatus.AWAITING_MATCH -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, status, ApprovedPremisesApplicationStatus.STARTED)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApprovedPremisesApplicationStatus::class)
  fun `should transform CAS1 application status`(status: ApprovedPremisesApplicationStatus) {
    val expected = when (status) {
      ApprovedPremisesApplicationStatus.EXPIRED -> AccommodationReferralStatus.EXPIRED
      ApprovedPremisesApplicationStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
      ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED -> AccommodationReferralStatus.ACCEPTED
      ApprovedPremisesApplicationStatus.REJECTED -> AccommodationReferralStatus.REJECTED
      ApprovedPremisesApplicationStatus.INAPPLICABLE -> AccommodationReferralStatus.REJECTED
      ApprovedPremisesApplicationStatus.STARTED -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.AWAITING_ASSESSMENT -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.UNALLOCATED_ASSESSMENT -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.ASSESSMENT_IN_PROGRESS -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.AWAITING_PLACEMENT -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.REQUESTED_FURTHER_INFORMATION -> AccommodationReferralStatus.PENDING
      ApprovedPremisesApplicationStatus.PENDING_PLACEMENT_REQUEST -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, null, status)).isEqualTo(expected)
  }

  @Test
  fun `should respect precedence of statuses for CAS1`() {
    assertThat(
      AccommodationReferralTransformer.toCasReferralStatus(
        Cas1SpaceBookingStatus.CANCELLED,
        RequestForPlacementStatus.PLACEMENT_BOOKED,
        ApprovedPremisesApplicationStatus.EXPIRED,
      ),
    ).isEqualTo(AccommodationReferralStatus.CANCELLED)

    assertThat(
      AccommodationReferralTransformer.toCasReferralStatus(
        null,
        RequestForPlacementStatus.REQUEST_REJECTED,
        ApprovedPremisesApplicationStatus.EXPIRED,
      ),
    ).isEqualTo(AccommodationReferralStatus.REQUEST_REJECTED)

    assertThat(
      AccommodationReferralTransformer.toCasReferralStatus(
        null,
        RequestForPlacementStatus.AWAITING_MATCH,
        ApprovedPremisesApplicationStatus.EXPIRED,
      ),
    ).isEqualTo(AccommodationReferralStatus.EXPIRED)
  }

  @ParameterizedTest
  @EnumSource(Cas3BookingStatus::class)
  fun `should transform CAS3 booking status`(status: Cas3BookingStatus) {
    val expected = when (status) {
      Cas3BookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
      Cas3BookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
      Cas3BookingStatus.NOT_MINUS_ARRIVED -> AccommodationReferralStatus.ACCEPTED
      Cas3BookingStatus.ARRIVED -> AccommodationReferralStatus.ACCEPTED
      Cas3BookingStatus.CONFIRMED -> AccommodationReferralStatus.ACCEPTED
      Cas3BookingStatus.PROVISIONAL -> AccommodationReferralStatus.PENDING
      Cas3BookingStatus.CLOSED -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(status, null, ApplicationStatus.SUBMITTED, null)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(AssessmentStatus::class)
  fun `should transform CAS3 assessment status`(status: AssessmentStatus) {
    val expected = when (status) {
      AssessmentStatus.REJECTED -> AccommodationReferralStatus.REJECTED
      AssessmentStatus.UNALLOCATED -> AccommodationReferralStatus.PENDING
      AssessmentStatus.IN_REVIEW -> AccommodationReferralStatus.PENDING
      AssessmentStatus.READY_TO_PLACE -> AccommodationReferralStatus.PENDING
      AssessmentStatus.CLOSED -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, status, ApplicationStatus.SUBMITTED, "reason")).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(AssessmentStatus::class)
  fun `should transform CAS3 assessment status without reason`(status: AssessmentStatus) {
    val expected = when (status) {
      AssessmentStatus.REJECTED -> AccommodationReferralStatus.ARCHIVED
      AssessmentStatus.UNALLOCATED -> AccommodationReferralStatus.PENDING
      AssessmentStatus.IN_REVIEW -> AccommodationReferralStatus.PENDING
      AssessmentStatus.READY_TO_PLACE -> AccommodationReferralStatus.PENDING
      AssessmentStatus.CLOSED -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, status, ApplicationStatus.SUBMITTED, null)).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApplicationStatus::class)
  fun `should transform CAS3 application status`(status: ApplicationStatus) {
    val expected = when (status) {
      ApplicationStatus.REJECTED -> AccommodationReferralStatus.REJECTED
      ApplicationStatus.IN_PROGRESS -> AccommodationReferralStatus.PENDING
      ApplicationStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
      ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, null, status, "reason")).isEqualTo(expected)
  }

  @ParameterizedTest
  @EnumSource(ApplicationStatus::class)
  fun `should transform CAS3 application status without reason`(status: ApplicationStatus) {
    val expected = when (status) {
      ApplicationStatus.REJECTED -> AccommodationReferralStatus.ARCHIVED
      ApplicationStatus.IN_PROGRESS -> AccommodationReferralStatus.PENDING
      ApplicationStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
      ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> AccommodationReferralStatus.PENDING
    }
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(null, null, status, null)).isEqualTo(expected)
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
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expected)
  }
}
