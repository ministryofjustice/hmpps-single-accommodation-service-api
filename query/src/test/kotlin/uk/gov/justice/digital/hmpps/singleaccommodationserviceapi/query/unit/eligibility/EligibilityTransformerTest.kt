package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PlacementApplicationDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawPlacementRequestReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1ApplicationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1AssessmentSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PlacementPairDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1RequestForPlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3ApplicationDto
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toFailedEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotRequiredServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus as InfraCas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus as InfraCas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus as InfraCas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus as InfraCas3ApplicationStatus

class EligibilityTransformerTest {

  @Test
  fun `should transform to eligibility`() {
    val today = LocalDate.now()
    val now = OffsetDateTime.now()
    val id = UUID.randomUUID()
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
        withdrawalReason = "relatedApplicationWithdrawn",
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
            withdrawalReason = "withdrawnByPP",
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
      applicationStatus = InfraCas3ApplicationStatus.IN_PROGRESS,
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
      id = cas3Application.id,
      applicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
    )
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val commissionedRehabilitativeServicesDto = buildCommissionedRehabilitativeServicesDto()
    val dutyToReferDto = buildDutyToReferDto()
    val data = buildDomainData(
      cas1Application = cas1Application,
      cas3Application = cas3Application,
      dutyToRefer = dutyToReferDto,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
    )
    val crn = "FAKECRN1"
    val crs = buildServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    )
    val cas1Action = CaseAction(type = CaseActionType.PROVIDE_INFORMATION)
    val dtrAction = CaseAction(type = CaseActionType.ADD_DTR_OUTCOME)
    val cas1 = buildServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      action = cas1Action,
      link = EligibilityKeys.VIEW_APPLICATION,
    )
    val cas3 = buildServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      link = EligibilityKeys.VIEW_REFERRAL,
    )
    val dtr = buildServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      action = dtrAction,
      link = EligibilityKeys.ADD_OUTCOME,
    )
    val pa = buildServiceResult(
      serviceStatus = ServiceStatus.COMPLETED,
    )

    val cas1ServiceResult = buildCas1ServiceResult(
      serviceResult = cas1,
      cas1Application = cas1ApplicationDto,
    )
    val cas3ServiceResult = buildCas3ServiceResult(
      serviceResult = cas3,
      cas3Application = cas3ApplicationDto,
    )
    val dtrServiceResult = buildDtrServiceResult(
      serviceResult = dtr,
      caseId = dutyToReferDto.caseId,
      submission = dutyToReferDto.submission,
    )
    val crsServiceResult = buildCrsServiceResult(
      serviceResult = crs,
      commissionedRehabilitativeServices = commissionedRehabilitativeServicesDto,
    )
    val paServiceResult = buildPaServiceResult(
      serviceResult = pa,
    )
    val caseActions = listOf(dtrAction, cas1Action)

    val expectedEligibility = buildEligibilityDto(
      crn = crn,
      cas1 = cas1ServiceResult,
      cas3 = cas3ServiceResult,
      dtr = dtrServiceResult,
      crs = crsServiceResult,
      pa = paServiceResult,
      caseActions = caseActions,
    )

    val actualEligibility = toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      crs = crs,
      pa = pa,
      data = data,
    )

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  @Test
  fun `sorts case actions by soonest start date first, with undated actions last`() {
    val cas1Action = CaseAction(type = CaseActionType.START_APPROVED_PREMISE_APPLICATION, startDate = LocalDate.of(2025, 12, 1))
    val crsAction = CaseAction(type = CaseActionType.SUBMIT_CRS_REFERRAL, startDate = LocalDate.of(2026, 9, 8))
    val cas3Action = CaseAction(type = CaseActionType.START_CAS3_REFERRAL, startDate = LocalDate.of(2026, 11, 3))
    val dtrAction = CaseAction(type = CaseActionType.ADD_DTR_OUTCOME, startDate = null)
    val paAction = CaseAction(type = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS, startDate = null)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(action = cas1Action),
      cas3 = buildServiceResult(action = cas3Action),
      dtr = buildServiceResult(action = dtrAction),
      crs = buildServiceResult(action = crsAction),
      pa = buildServiceResult(action = paAction),
      data = buildDomainData(),
    )

    assertThat(actualEligibility.caseActions).containsExactly(cas1Action, crsAction, cas3Action, dtrAction, paAction)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ServiceStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `does not surface the DTR submission unless the DTR result is SUBMITTED, ACCEPTED or NOT ACCEPTED`(serviceStatus: ServiceStatus) {
    val dutyToReferDto = buildDutyToReferDto(status = DtrStatus.WITHDRAWN)
    val data = buildDomainData(dutyToRefer = dutyToReferDto)
    val dtr = buildServiceResult(
      serviceStatus = serviceStatus,
      action = CaseAction(type = CaseActionType.ADD_DTR_REFERRAL_DETAILS),
      link = EligibilityKeys.ADD_REFERRAL_DETAILS,
    )

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(),
      cas3 = buildServiceResult(),
      dtr = dtr,
      crs = buildServiceResult(),
      pa = buildServiceResult(),
      data = data,
    )

    assertThat(actualEligibility.dtr.submission).isNull()
    assertThat(actualEligibility.dtr.caseId).isEqualTo(dutyToReferDto.caseId)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ServiceStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `surfaces the DTR submission when the DTR result is SUBMITTED, ACCEPTED or NOT ACCEPTED`(serviceStatus: ServiceStatus) {
    val dutyToReferDto = buildDutyToReferDto()
    val data = buildDomainData(dutyToRefer = dutyToReferDto)
    val dtr = buildServiceResult(serviceStatus = serviceStatus)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(),
      cas3 = buildServiceResult(),
      dtr = dtr,
      crs = buildServiceResult(),
      pa = buildServiceResult(),
      data = data,
    )

    assertThat(actualEligibility.dtr.submission).isEqualTo(dutyToReferDto.submission)
    assertThat(actualEligibility.dtr.caseId).isEqualTo(dutyToReferDto.caseId)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ServiceStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `does not surface the CRS referral data unless the CRS result is SUBMITTED, ACCEPTED or NOT ACCEPTED`(serviceStatus: ServiceStatus) {
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val data = buildDomainData(commissionedRehabilitativeServices = commissionedRehabilitativeServices)
    val crs = buildServiceResult(
      serviceStatus = serviceStatus,
      action = CaseAction(type = CaseActionType.SUBMIT_CRS_REFERRAL),
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    )

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(),
      cas3 = buildServiceResult(),
      dtr = buildServiceResult(),
      crs = crs,
      pa = buildServiceResult(),
      data = data,
    )

    assertThat(actualEligibility.crs.commissionedRehabilitativeServices).isNull()
  }

  @Test
  fun `surfaces the CRS referral data when the CRS result is SUBMITTED`() {
    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val commissionedRehabilitativeServicesDto = buildCommissionedRehabilitativeServicesDto()
    val data = buildDomainData(commissionedRehabilitativeServices = commissionedRehabilitativeServices)
    val crs = buildServiceResult(serviceStatus = ServiceStatus.SUBMITTED)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(),
      cas3 = buildServiceResult(),
      dtr = buildServiceResult(),
      crs = crs,
      pa = buildServiceResult(),
      data = data,
    )

    assertThat(actualEligibility.crs.commissionedRehabilitativeServices).isEqualTo(commissionedRehabilitativeServicesDto)
  }

  @Test
  fun `should transform to failed eligibility`() {
    val crn = "FAKECRN1"
    val expectedEligibility = buildEligibilityDto(crn)

    val actualEligibility = toFailedEligibilityDto(crn)

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  @Test
  fun `should transform to not eligible service status`() {
    val expectedServiceStatus = buildServiceResult()

    val actualEligibility = toNotEligibleServiceStatus()

    assertThat(actualEligibility).isEqualTo(expectedServiceStatus)
  }

  @Test
  fun `should transform to not required service status`() {
    val expectedServiceStatus = buildServiceResult(ServiceStatus.NOT_REQUIRED)

    val actualEligibility = toNotRequiredServiceStatus()

    assertThat(actualEligibility).isEqualTo(expectedServiceStatus)
  }

  @Test
  fun `does not include case actions for non actionable statuses`() {
    val nonActionableCas3Action = CaseAction(type = CaseActionType.SUBMIT_DTR_BEFORE_CAS3)
    val actionableDtrAction = CaseAction(type = CaseActionType.ADD_DTR_REFERRAL_DETAILS)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(),
      cas3 = buildServiceResult(serviceStatus = ServiceStatus.CANNOT_START_YET, action = nonActionableCas3Action),
      dtr = buildServiceResult(serviceStatus = ServiceStatus.NOT_STARTED, action = actionableDtrAction),
      crs = buildServiceResult(),
      pa = buildServiceResult(),
      data = buildDomainData(),
    )

    assertThat(actualEligibility.caseActions).containsExactly(actionableDtrAction)
  }

  @Test
  fun `does not include null actions when building case actions`() {
    val actionablePaAction = CaseAction(type = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS)

    val actualEligibility = toEligibilityDto(
      crn = "FAKECRN1",
      cas1 = buildServiceResult(serviceStatus = ServiceStatus.SUBMITTED, action = null),
      cas3 = buildServiceResult(serviceStatus = ServiceStatus.SUBMITTED, action = null),
      dtr = buildServiceResult(serviceStatus = ServiceStatus.SUBMITTED, action = null),
      crs = buildServiceResult(serviceStatus = ServiceStatus.SUBMITTED, action = null),
      pa = buildServiceResult(serviceStatus = ServiceStatus.NOT_STARTED, action = actionablePaAction),
      data = buildDomainData(),
    )

    assertThat(actualEligibility.caseActions).containsExactly(actionablePaAction)
  }
}
