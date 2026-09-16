package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PlacementApplicationDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawPlacementRequestReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1ApplicationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1AssessmentSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PlacementPairDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1RequestForPlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3ExternalPreviousBookingCancellationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3ExternalPreviousBookingDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityNewToOldTransformer.toEligibilityDtoOld
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityNewToOldTransformer.toServiceStatusOld
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas2ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas3ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCrsServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDtrServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPaServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.stream.Stream

class EligibilityNewToOldTransformerTest {

  @ParameterizedTest
  @MethodSource("serviceStatuses")
  fun `should transform to old service status`(
    serviceStatus: ServiceStatusNew,
    serviceStatusOld: ServiceStatus,
  ) {
    val result = toServiceStatusOld(
      serviceStatus = serviceStatus,
    )

    assertThat(result).isEqualTo(serviceStatusOld)
  }

  private companion object {
    @JvmStatic
    fun serviceStatuses(): Stream<Arguments> = Stream.of(
      Arguments.of(ServiceStatusNew.CAS1_APPLICATION_REJECTED, ServiceStatus.APPLICATION_REJECTED),
      Arguments.of(ServiceStatusNew.CAS1_ARRIVED, ServiceStatus.ARRIVED),
      Arguments.of(ServiceStatusNew.CAS1_INFO_REQUESTED, ServiceStatus.INFO_REQUESTED),
      Arguments.of(ServiceStatusNew.CAS1_NOT_ARRIVED, ServiceStatus.NOT_ARRIVED),
      Arguments.of(ServiceStatusNew.CAS1_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.CAS1_NOT_STARTED, ServiceStatus.NOT_STARTED),
      Arguments.of(ServiceStatusNew.CAS1_NOT_SUBMITTED, ServiceStatus.NOT_SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_BOOKED, ServiceStatus.PLACEMENT_BOOKED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_CANCELLED, ServiceStatus.PLACEMENT_CANCELLED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED, ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_REJECTED, ServiceStatus.PLACEMENT_REQUEST_REJECTED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_SUBMITTED, ServiceStatus.PLACEMENT_REQUEST_SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_WITHDRAWN, ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN),
      Arguments.of(ServiceStatusNew.CAS1_SUBMITTED, ServiceStatus.SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS1_UPCOMING, ServiceStatus.UPCOMING),

      Arguments.of(ServiceStatusNew.CAS2_AWAITING_ARRIVAL, ServiceStatus.AWAITING_ARRIVAL),
      Arguments.of(ServiceStatusNew.CAS2_AWAITING_DECISION, ServiceStatus.AWAITING_DECISION),
      Arguments.of(ServiceStatusNew.CAS2_CANCELLED, ServiceStatus.CANCELLED),
      Arguments.of(ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED, ServiceStatus.MORE_INFORMATION_NEEDED),
      Arguments.of(ServiceStatusNew.CAS2_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.CAS2_NOT_STARTED, ServiceStatus.NOT_STARTED),
      Arguments.of(ServiceStatusNew.CAS2_NOT_SUBMITTED, ServiceStatus.NOT_SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS2_OFFER_ACCEPTED, ServiceStatus.OFFER_ACCEPTED),
      Arguments.of(ServiceStatusNew.CAS2_OFFER_DECLINED_OR_WITHDRAWN, ServiceStatus.OFFER_DECLINED_OR_WITHDRAWN),
      Arguments.of(ServiceStatusNew.CAS2_ON_WAITING_LIST, ServiceStatus.ON_WAITING_LIST),
      Arguments.of(ServiceStatusNew.CAS2_PLACE_OFFERED, ServiceStatus.PLACE_OFFERED),
      Arguments.of(ServiceStatusNew.CAS2_SUBMITTED, ServiceStatus.SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS2_UNKNOWN, ServiceStatus.UNKNOWN),
      Arguments.of(ServiceStatusNew.CAS2_UPCOMING, ServiceStatus.UPCOMING),
      Arguments.of(ServiceStatusNew.CAS2_WITHDRAWN, ServiceStatus.WITHDRAWN),

      Arguments.of(ServiceStatusNew.CAS3_BEDSPACE_OFFERED, ServiceStatus.BEDSPACE_OFFERED),
      Arguments.of(ServiceStatusNew.CAS3_BOOKING_CANCELLED, ServiceStatus.BOOKING_CANCELLED),
      Arguments.of(ServiceStatusNew.CAS3_BOOKING_CONFIRMED, ServiceStatus.BOOKING_CONFIRMED),
      Arguments.of(ServiceStatusNew.CAS3_CANNOT_START_YET, ServiceStatus.CANNOT_START_YET),
      Arguments.of(ServiceStatusNew.CAS3_NOT_ARRIVED, ServiceStatus.NOT_ARRIVED),
      Arguments.of(ServiceStatusNew.CAS3_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.CAS3_NOT_STARTED, ServiceStatus.NOT_STARTED),
      Arguments.of(ServiceStatusNew.CAS3_NOT_SUBMITTED, ServiceStatus.NOT_SUBMITTED),
      Arguments.of(ServiceStatusNew.CAS3_REJECTED, ServiceStatus.REJECTED),
      Arguments.of(ServiceStatusNew.CAS3_SUBMITTED, ServiceStatus.SUBMITTED),

      Arguments.of(ServiceStatusNew.CRS_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.CRS_NOT_REQUIRED, ServiceStatus.NOT_REQUIRED),
      Arguments.of(ServiceStatusNew.CRS_NOT_STARTED, ServiceStatus.NOT_STARTED),
      Arguments.of(ServiceStatusNew.CRS_SUBMITTED, ServiceStatus.SUBMITTED),
      Arguments.of(ServiceStatusNew.CRS_UPCOMING, ServiceStatus.UPCOMING),

      Arguments.of(ServiceStatusNew.DTR_ACCEPTED, ServiceStatus.ACCEPTED),
      Arguments.of(ServiceStatusNew.DTR_NOT_ACCEPTED, ServiceStatus.NOT_ACCEPTED),
      Arguments.of(ServiceStatusNew.DTR_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.DTR_NOT_REQUIRED, ServiceStatus.NOT_REQUIRED),
      Arguments.of(ServiceStatusNew.DTR_NOT_STARTED, ServiceStatus.NOT_STARTED),
      Arguments.of(ServiceStatusNew.DTR_SUBMITTED, ServiceStatus.SUBMITTED),
      Arguments.of(ServiceStatusNew.DTR_UPCOMING, ServiceStatus.UPCOMING),

      Arguments.of(ServiceStatusNew.PA_COMPLETED, ServiceStatus.COMPLETED),
      Arguments.of(ServiceStatusNew.PA_NOT_ELIGIBLE, ServiceStatus.NOT_ELIGIBLE),
      Arguments.of(ServiceStatusNew.PA_NOT_STARTED, ServiceStatus.NOT_STARTED),
    )
  }

  @Test
  fun `should transform to old eligibility`() {
    val today = LocalDate.now()
    val now = OffsetDateTime.now()

    val cas2ApplicationDto = buildCas2ApplicationDto()
    val cas1ApplicationDto = buildCas1ApplicationDto(
      application = buildCas1ApplicationSummaryDto(
        status = Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        createdAt = now.plusDays(1),
        createdBy = Cas1StaffDto(
          name = "Bob",
          username = "bob1234",
          staffCode = "1234",
        ),
        submittedAt = now.plusDays(2),
        expiresAt = today.plusDays(3),
      ),
      uiUrl = "https://cas1.example.com/applications/1234",
      assessment = buildCas1AssessmentSummaryDto(
        decision = AssessmentDecision.ACCEPTED,
        rejectionRationale = "rejection rationale",
      ),
      requestForPlacement = buildCas1RequestForPlacementSummaryDto(
        status = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        decision = PlacementApplicationDecision.ACCEPTED,
        rejectionReason = "rejection reason",
        submittedBy = buildCas1StaffDto(
          name = "Anne",
          username = "anne5678",
          staffCode = "5678",
        ),
        submittedAt = today.plusDays(4),
        withdrawalReason = WithdrawPlacementRequestReason.RELATED_APPLICATION_WITHDRAWN,
        withdrawalDate = today.plusDays(5),
        expectedArrivalDate = today.plusDays(6),
        durationDays = 12,
      ),
      placement = buildCas1PlacementSummaryDto(
        status = Cas1PlacementStatus.DEPARTED,
        actualArrivalDate = today.plusDays(7),
        actualDepartureDate = today.plusDays(8),
        cancellationReason = "cancellation reason",
        premises = Cas1PremisesSummaryDto(
          startDate = today.plusDays(9),
          endDate = today.plusDays(10),
          addressLine1 = "123 Main St",
          addressLine2 = "Apt 1",
          town = "London",
          postcode = "SW1A 1AA",
        ),
      ),
      placementHistory = listOf(
        buildCas1PlacementPairDto(
          requestForPlacement = buildCas1RequestForPlacementSummaryDto(
            status = Cas1RequestForPlacementStatus.REQUEST_REJECTED,
            decision = PlacementApplicationDecision.REJECTED,
            rejectionReason = "rejection reason 2",
            submittedBy = Cas1StaffDto(
              name = "Carl",
              username = "carl9999",
              staffCode = "9999",
            ),
            submittedAt = today.plusDays(11),
            withdrawalReason = WithdrawPlacementRequestReason.WITHDRAWN_BY_PP,
            withdrawalDate = today.plusDays(12),
            expectedArrivalDate = today.plusDays(13),
            durationDays = 14,
          ),
          placement = buildCas1PlacementSummaryDto(
            status = Cas1PlacementStatus.UPCOMING,
            actualArrivalDate = today.plusDays(14),
            actualDepartureDate = today.plusDays(15),
            cancellationReason = "cancellation reason 2",
            premises = buildCas1PremisesSummaryDto(
              startDate = today.plusDays(16),
              endDate = today.plusDays(17),
              addressLine1 = "123 Main St 2",
              addressLine2 = "Apt 2",
              town = "London 2",
              postcode = "SW1A 1AB",
            ),
          ),
          dateApplied = today.plusDays(18),
        ),
      ),
    )
    val cas3ApplicationDto = buildCas3ApplicationDto(
      applicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
      applicationSubmittedDate = LocalDate.parse("2023-01-01"),
      applicationSubmittedBy = buildCas3StaffDto(),
      applicationRejectedReason = "Problem with application",
      assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
      bookingStatus = Cas3BookingStatus.NOT_MINUS_ARRIVED,
      bookingProvisionalOfferSentDate = LocalDate.parse("2023-01-02"),
      previousBookings = listOf(
        buildCas3ExternalPreviousBookingDto(
          bookingStatus = Cas3BookingStatus.DEPARTED,
          cancellation = buildCas3ExternalPreviousBookingCancellationDto(
            cancellationDate = LocalDate.parse("2023-01-03"),
            cancellationReason = "Booking cancelled",
          ),
        ),
      ),
      premises = buildCas3PremisesSummaryDto(
        name = "123 Main St",
        startDate = LocalDate.parse("2023-01-04"),
        endDate = LocalDate.parse("2023-01-05"),
        addressLine1 = "124 Main St",
        addressLine2 = "Apt 1",
        town = "Lincoln",
        postcode = "SW1A 1AX",
      ),
      uiUrl = "aUrl",
    )
    val commissionedRehabilitativeServicesDto = buildCommissionedRehabilitativeServicesDto()
    val dutyToReferDto = buildDutyToReferDto()

    val crn = "FAKECRN1"

    val cas1Action = CaseAction(type = CaseActionType.PROVIDE_INFORMATION, service = AccommodationService.CAS1)
    val cas2Action = CaseAction(type = CaseActionType.START_CAS2_REFERRAL, service = AccommodationService.CAS2)
    val cas3Action = CaseAction(type = CaseActionType.START_CAS3_REFERRAL, service = AccommodationService.CAS3)
    val dtrAction = CaseAction(type = CaseActionType.ADD_DTR_OUTCOME, service = AccommodationService.DTR)
    val crsAction = CaseAction(type = CaseActionType.SUBMIT_CRS_REFERRAL, service = AccommodationService.CRS)
    val paAction = CaseAction(type = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS, service = AccommodationService.PA)
    val crs = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CRS_SUBMITTED,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      action = crsAction,
      linkType = null,
      url = "crs/test",
    )
    val cas1 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_INFO_REQUESTED,
      action = cas1Action,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
      url = "cas1/test",
    )
    val cas2 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_AWAITING_ARRIVAL,
      action = cas2Action,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
      url = "cas2/test",
    )
    val cas3 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS3_NOT_SUBMITTED,
      action = cas3Action,
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
      url = "cas3/test",
    )
    val dtr = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_SUBMITTED,
      action = dtrAction,
      link = EligibilityKeys.ADD_OUTCOME,
      linkType = null,
      url = "dtr/test",
    )
    val pa = buildServiceResultNew(
      action = paAction,
      serviceStatus = ServiceStatusNew.PA_COMPLETED,
      linkType = null,
      url = "pa/test",
    )

    val cas1ServiceResult = buildCas1ServiceResultNew(
      serviceResult = cas1,
      cas1Application = cas1ApplicationDto,
    )
    val cas2ServiceResult = buildCas2ServiceResultNew(
      serviceResult = cas2,
      cas2Application = cas2ApplicationDto,
    )
    val cas3ServiceResult = buildCas3ServiceResultNew(
      serviceResult = cas3,
      cas3Application = cas3ApplicationDto,
    )
    val dtrServiceResult = buildDtrServiceResultNew(
      serviceResult = dtr,
      caseId = dutyToReferDto.caseId,
      submission = dutyToReferDto.submission,
    )
    val crsServiceResult = buildCrsServiceResultNew(
      serviceResult = crs,
      commissionedRehabilitativeServices = commissionedRehabilitativeServicesDto,
    )
    val paServiceResult = buildPaServiceResultNew(
      serviceResult = pa,
    )
    val caseActions = listOf(dtrAction, cas1Action, cas2Action)

    val eligibilityDtoOld = EligibilityDto(
      crn = crn,
      cas1 = Cas1ServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.INFO_REQUESTED,
          action = cas1Action,
          link = cas1.link,
          url = cas1.url,
          linkType = cas1.linkType,
          failureReasons = cas1.failureReasons,
          blockingStatusReason = cas1.blockingStatusReason,
        ),
        cas1Application = cas1ApplicationDto,
      ),
      cas2 = Cas2ServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.AWAITING_ARRIVAL,
          action = cas2Action,
          link = cas2.link,
          url = cas2.url,
          linkType = cas2.linkType,
          failureReasons = cas2.failureReasons,
          blockingStatusReason = cas2.blockingStatusReason,
        ),
        cas2Application = cas2ApplicationDto,
      ),
      cas3 = Cas3ServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.NOT_SUBMITTED,
          action = cas3Action,
          link = cas3.link,
          url = cas3.url,
          linkType = cas3.linkType,
          failureReasons = cas3.failureReasons,
          blockingStatusReason = cas3.blockingStatusReason,
        ),
        cas3Application = cas3ApplicationDto,
      ),
      dtr = DtrServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          action = dtrAction,
          link = dtr.link,
          url = dtr.url,
          linkType = dtr.linkType,
          failureReasons = dtr.failureReasons,
          blockingStatusReason = dtr.blockingStatusReason,
        ),
        caseId = dutyToReferDto.caseId,
        submission = dutyToReferDto.submission,
      ),
      crs = CrsServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.CANCELLED,
          action = crsAction,
          link = crs.link,
          url = crs.url,
          linkType = crs.linkType,
          failureReasons = crs.failureReasons,
          blockingStatusReason = crs.blockingStatusReason,
        ),
        commissionedRehabilitativeServices = commissionedRehabilitativeServicesDto,
      ),
      pa = PaServiceResult(
        serviceResult = ServiceResult(
          serviceStatus = ServiceStatus.COMPLETED,
          action = paAction,
          link = pa.link,
          url = pa.url,
          linkType = pa.linkType,
          failureReasons = pa.failureReasons,
          blockingStatusReason = pa.blockingStatusReason,
        ),
      ),
      caseActions = caseActions,
    )

    val eligibilityDto = buildEligibilityDtoNew(
      crn = crn,
      cas1 = cas1ServiceResult,
      cas2 = cas2ServiceResult,
      cas3 = cas3ServiceResult,
      dtr = dtrServiceResult,
      crs = crsServiceResult,
      pa = paServiceResult,
      caseActions = caseActions,
    )

    val result = toEligibilityDtoOld(
      eligibilityDto,
    )

    assertThat(result).isEqualTo(eligibilityDtoOld)
  }
}
