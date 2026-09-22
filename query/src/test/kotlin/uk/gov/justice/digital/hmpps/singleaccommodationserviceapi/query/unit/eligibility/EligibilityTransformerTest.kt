package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PlacementApplicationDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1AssessmentSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementPair
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3ExternalPreviousBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3ExternalPreviousBookingCancellation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.getServiceResultActionOrder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toFailedEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotRequiredServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas2ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas3ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCrsServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDtrServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPaServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus as InfraCas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus as InfraCas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus as InfraCas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus as InfraCas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus as InfraCas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus as InfraCas3BookingStatus

class EligibilityTransformerTest {

  @Nested
  inner class GetServiceResultActionOrder {
    @Test
    fun `should place in standard (DTR, CRS, CAS1, CAS2, CAS3, PA) order when status is not upcoming`() {
      val dtr = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.DTR_NOT_STARTED,
      )
      val crs = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_REFERRAL,
      )
      val cas1 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED,
      )
      val cas2 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED,
      )
      val cas3 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED,
      )
      val pa = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.PA_NOT_STARTED,
      )

      val expectedServiceResultOrder = listOf(
        dtr,
        crs,
        cas1,
        cas2,
        cas3,
        pa,
      )

      val result = getServiceResultActionOrder(
        cas1 = cas1,
        cas2 = cas2,
        cas3 = cas3,
        dtr = dtr,
        crs = crs,
        pa = pa,
      )

      assertThat(result).isEqualTo(expectedServiceResultOrder)
    }

    @Test
    fun `should miss out services with no action required`() {
      val dtr = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.DTR_ACCEPTED,
      )
      val crs = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CRS_SUBMITTED,
      )
      val cas1 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_SUBMITTED,
      )
      val cas2 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS2_SUBMITTED,
      )
      val cas3 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS3_SUBMITTED,
      )
      val pa = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.PA_COMPLETED,
      )

      val expectedServiceResultOrder = emptyList<ServiceResultNew>()

      val result = getServiceResultActionOrder(
        cas1 = cas1,
        cas2 = cas2,
        cas3 = cas3,
        dtr = dtr,
        crs = crs,
        pa = pa,
      )

      assertThat(result).isEqualTo(expectedServiceResultOrder)
    }

    @Test
    fun `should place in ascending order by start date when statuses are upcoming and then continue standard order for others`() {
      val dtr = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.DTR_UPCOMING,
        actionStartDate = LocalDate.of(2023, 1, 4),
      )
      val crs = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CRS_UPCOMING_REFERRAL,
        actionStartDate = LocalDate.of(2023, 1, 3),
      )
      val cas1 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_UPCOMING,
        actionStartDate = LocalDate.of(2023, 1, 2),
      )
      val cas2 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS2_UPCOMING,
        actionStartDate = LocalDate.of(2023, 1, 1),
      )
      val cas3 = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED,
      )
      val pa = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.PA_NOT_STARTED,
      )

      val expectedServiceResultOrder = listOf(
        cas2,
        cas1,
        crs,
        dtr,
        cas3,
        pa,
      )

      val result = getServiceResultActionOrder(
        cas1 = cas1,
        cas2 = cas2,
        cas3 = cas3,
        dtr = dtr,
        crs = crs,
        pa = pa,
      )

      assertThat(result).isEqualTo(expectedServiceResultOrder)
    }
  }

  @Test
  fun `should transform to eligibility`() {
    val today = LocalDate.now()
    val now = OffsetDateTime.now()
    val id = UUID.randomUUID()
    val cas2Application = buildCas2Application()
    val cas1Application = buildCas1Application(
      application = buildCas1ApplicationSummary(
        status = InfraCas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        id = id,
        createdAt = now.plusDays(1),
        createdBy = Cas1Staff(
          name = "Bob",
          username = "bob1234",
          staffCode = "1234",
        ),
        submittedAt = now.plusDays(2),
        expiresAt = today.plusDays(3),
      ),
      uiUrl = "https://cas1.example.com/applications/1234",
      assessment = buildCas1AssessmentSummary(
        decision = "accepted",
        rejectionRationale = "rejection rationale",
      ),
      requestForPlacement = buildCas1RequestForPlacementSummary(
        status = InfraCas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        decision = "accepted",
        rejectionReason = "rejection reason",
        submittedBy = buildCas1Staff(
          name = "Anne",
          username = "anne5678",
          staffCode = "5678",
        ),
        submittedAt = today.plusDays(4),
        withdrawalReason = "RelatedApplicationWithdrawn",
        withdrawalDate = today.plusDays(5),
        expectedArrivalDate = today.plusDays(6),
        durationDays = 12,
      ),
      placement = buildCas1PlacementSummary(
        status = InfraCas1PlacementStatus.DEPARTED,
        actualArrivalDate = today.plusDays(7),
        actualDepartureDate = today.plusDays(8),
        cancellationReason = "cancellation reason",
        premises = buildCas1PremisesSummary(
          startDate = today.plusDays(9),
          endDate = today.plusDays(10),
          addressLine1 = "123 Main St",
          addressLine2 = "Apt 1",
          town = "London",
          postcode = "SW1A 1AA",
        ),
      ),
      placementHistory = listOf(
        buildCas1PlacementPair(
          requestForPlacement = buildCas1RequestForPlacementSummary(
            status = InfraCas1RequestForPlacementStatus.REQUEST_REJECTED,
            decision = "rejected",
            rejectionReason = "rejection reason 2",
            submittedBy = Cas1Staff(
              name = "Carl",
              username = "carl9999",
              staffCode = "9999",
            ),
            submittedAt = today.plusDays(11),
            withdrawalReason = "WithdrawnByPP",
            withdrawalDate = today.plusDays(12),
            expectedArrivalDate = today.plusDays(13),
            durationDays = 14,
          ),
          placement = buildCas1PlacementSummary(
            status = InfraCas1PlacementStatus.UPCOMING,
            actualArrivalDate = today.plusDays(14),
            actualDepartureDate = today.plusDays(15),
            cancellationReason = "cancellation reason 2",
            premises = buildCas1PremisesSummary(
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
    val cas3Application = buildCas3Application(
      applicationStatus = InfraCas3ApplicationStatus.SUBMITTED,
      id = UUID.randomUUID(),
      submittedApplication = buildCas3SubmittedApplicationDto(
        submittedDate = LocalDate.parse("2023-01-01"),
        submittedBy = buildCas3Staff(),
        assessmentStatus = InfraCas3AssessmentStatus.REJECTED,
        assessmentRejectionReason = "Problem with application",
        latestBooking = buildCas3LatestBooking(
          status = InfraCas3BookingStatus.NOT_MINUS_ARRIVED,
          provisionalOfferSentDate = LocalDate.parse("2023-01-02"),
          premises = buildCas3PremisesSummary(
            name = "123 Main St",
            startDate = LocalDate.parse("2023-01-04"),
            endDate = LocalDate.parse("2023-01-05"),
            addressLine1 = "124 Main St",
            addressLine2 = "Apt 1",
            town = "Lincoln",
            postcode = "SW1A 1AX",
          ),
        ),
        previousBookings = listOf(
          buildCas3ExternalPreviousBooking(
            bookingStatus = InfraCas3BookingStatus.DEPARTED,
            cancellation = buildCas3ExternalPreviousBookingCancellation(
              cancellationDate = LocalDate.parse("2023-01-03"),
              cancellationReason = "Booking cancelled",
            ),
          ),
        ),
      ),
      uiUrl = "aUrl",
    )
    val cas2ApplicationDto = buildCas2ApplicationDto(
      id = cas2Application.id,
      uiUrl = cas2Application.uiUrl,
    )
    val cas1ApplicationDto = buildCas1ApplicationDto(
      application = buildCas1ApplicationSummaryDto(
        status = Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        id = id,
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
      applicationStatus = Cas3ApplicationStatus.SUBMITTED,
      id = cas3Application.id,
      applicationSubmittedDate = LocalDate.parse("2023-01-01"),
      applicationSubmittedBy = buildCas3StaffDto(),
      applicationRejectedReason = "Problem with application",
      assessmentStatus = Cas3AssessmentStatus.REJECTED,
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
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val commissionedRehabilitativeServicesDto = buildCommissionedRehabilitativeServicesDto()
    val dutyToReferDto = buildDutyToReferDto()
    val data = buildDomainData(
      cas1Application = cas1Application,
      cas2Application = cas2Application,
      cas3Application = cas3Application,
      dutyToRefer = dutyToReferDto,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
    )
    val crn = "FAKECRN1"
    val crs = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CRS_SUBMITTED,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    )
    val cas1 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_INFO_REQUESTED,
      link = EligibilityKeys.VIEW_APPLICATION,
    )
    val cas2 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED,
      link = EligibilityKeys.VIEW_APPLICATION,
    )
    val cas3 = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS3_NOT_SUBMITTED,
      link = EligibilityKeys.VIEW_REFERRAL,
    )
    val dtr = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.DTR_SUBMITTED,
      link = EligibilityKeys.ADD_OUTCOME,
    )
    val pa = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.PA_COMPLETED,
    )

    val cas1ServiceResult = buildCas1ServiceResultNew(
      serviceResult = cas1,
      cas1Application = cas1ApplicationDto,
      actionPosition = 1,
    )
    val cas2ServiceResult = buildCas2ServiceResultNew(
      serviceResult = cas2,
      cas2Application = cas2ApplicationDto,
      actionPosition = 2,
    )
    val cas3ServiceResult = buildCas3ServiceResultNew(
      serviceResult = cas3,
      cas3Application = cas3ApplicationDto,
      actionPosition = 3,
    )
    val dtrServiceResult = buildDtrServiceResultNew(
      serviceResult = dtr,
      caseId = dutyToReferDto.caseId,
      submission = dutyToReferDto.submission,
      actionPosition = 0,
    )
    val crsServiceResult = buildCrsServiceResultNew(
      serviceResult = crs,
      commissionedRehabilitativeServices = commissionedRehabilitativeServicesDto,
      actionPosition = -1,
    )
    val paServiceResult = buildPaServiceResultNew(
      serviceResult = pa,
      actionPosition = -1,
    )

    val expectedEligibility = buildEligibilityDtoNew(
      crn = crn,
      cas1 = cas1ServiceResult,
      cas2 = cas2ServiceResult,
      cas3 = cas3ServiceResult,
      dtr = dtrServiceResult,
      crs = crsServiceResult,
      pa = paServiceResult,
    )

    val actualEligibility = toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2 = cas2,
      cas3 = cas3,
      dtr = dtr,
      crs = crs,
      pa = pa,
      data = data,
    )

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ServiceStatusNew::class, names = ["DTR_SUBMITTED", "DTR_ACCEPTED", "DTR_NOT_ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `does not surface the DTR submission unless the DTR result is SUBMITTED, ACCEPTED or NOT ACCEPTED`(serviceStatus: ServiceStatusNew) {
    val dutyToReferDto = buildDutyToReferDto(status = DtrStatus.WITHDRAWN)
    val data = buildDomainData(dutyToRefer = dutyToReferDto)

    val dtr = if (serviceStatus.isUpcoming) {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        link = EligibilityKeys.ADD_REFERRAL_DETAILS,
        actionStartDate = LocalDate.parse("2023-01-01"),
      )
    } else {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        link = EligibilityKeys.ADD_REFERRAL_DETAILS,
      )
    }

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResultNew(),
      cas2 = buildServiceResultNew(),
      cas3 = buildServiceResultNew(),
      dtr = dtr,
      crs = buildServiceResultNew(),
      pa = buildServiceResultNew(),
      data = data,
    )

    assertThat(actualEligibility.dtr.submission).isNull()
    assertThat(actualEligibility.dtr.caseId).isEqualTo(dutyToReferDto.caseId)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ServiceStatusNew::class, names = ["DTR_SUBMITTED", "DTR_ACCEPTED", "DTR_NOT_ACCEPTED"])
  fun `surfaces the DTR submission when the DTR result is SUBMITTED, ACCEPTED or NOT ACCEPTED`(serviceStatus: ServiceStatusNew) {
    val dutyToReferDto = buildDutyToReferDto()
    val data = buildDomainData(dutyToRefer = dutyToReferDto)
    val dtr = buildServiceResultNew(serviceStatus = serviceStatus)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResultNew(),
      cas2 = buildServiceResultNew(),
      cas3 = buildServiceResultNew(),
      dtr = dtr,
      crs = buildServiceResultNew(),
      pa = buildServiceResultNew(),
      data = data,
    )

    assertThat(actualEligibility.dtr.submission).isEqualTo(dutyToReferDto.submission)
    assertThat(actualEligibility.dtr.caseId).isEqualTo(dutyToReferDto.caseId)
  }

  @Test
  fun `does not surface the CRS referral data unless the CRS result is SUBMITTED`() {
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val data = buildDomainData(commissionedRehabilitativeServices = commissionedRehabilitativeServices)
    val crs = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CRS_UPCOMING_ACCOMMODATION_REFERRAL,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      actionStartDate = LocalDate.parse("2023-01-01"),
    )

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResultNew(),
      cas2 = buildServiceResultNew(),
      cas3 = buildServiceResultNew(),
      dtr = buildServiceResultNew(),
      crs = crs,
      pa = buildServiceResultNew(),
      data = data,
    )

    assertThat(actualEligibility.crs.commissionedRehabilitativeServices).isNull()
  }

  @Test
  fun `surfaces the CRS referral data when the CRS result is SUBMITTED`() {
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val commissionedRehabilitativeServicesDto = buildCommissionedRehabilitativeServicesDto()
    val data = buildDomainData(commissionedRehabilitativeServices = commissionedRehabilitativeServices)
    val crs = buildServiceResultNew(serviceStatus = ServiceStatusNew.CRS_SUBMITTED)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResultNew(),
      cas2 = buildServiceResultNew(),
      cas3 = buildServiceResultNew(),
      dtr = buildServiceResultNew(),
      crs = crs,
      pa = buildServiceResultNew(),
      data = data,
    )

    assertThat(actualEligibility.crs.commissionedRehabilitativeServices).isEqualTo(commissionedRehabilitativeServicesDto)
  }

  @Test
  fun `should transform to failed eligibility`() {
    val crn = "FAKECRN1"
    val expectedEligibility = buildEligibilityDtoNew(crn)

    val actualEligibility = toFailedEligibilityDto(crn)

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  @Test
  fun `should transform to not eligible service status`() {
    val expectedServiceStatus = buildServiceResultNew()

    val actualEligibility = toNotEligibleServiceStatus(AccommodationService.CAS1)

    assertThat(actualEligibility).isEqualTo(expectedServiceStatus)
  }

  @Test
  fun `should transform to not required service status`() {
    val expectedServiceStatus = buildServiceResultNew(ServiceStatusNew.DTR_NOT_REQUIRED)

    val actualEligibility = toNotRequiredServiceStatus(AccommodationService.DTR)

    assertThat(actualEligibility).isEqualTo(expectedServiceStatus)
  }
}
