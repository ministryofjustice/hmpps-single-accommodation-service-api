package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toFailedEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus as InfraCas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus as InfraCas3ApplicationStatus

class EligibilityTransformerTest {

  @Test
  fun `should transform to eligibility`() {
    val cas1Application = buildCas1Application(
      applicationStatus = InfraCas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
    )
    val cas3Application = buildCas3Application(
      applicationStatus = InfraCas3ApplicationStatus.IN_PROGRESS,
    )
    val cas1ApplicationDto = buildCas1ApplicationDto(
      id = cas1Application.id,
      applicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
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
  @EnumSource(value = ServiceStatus::class, names = ["NOT_STARTED", "NOT_ELIGIBLE"])
  fun `does not surface the DTR submission when the DTR result is NOT STARTED or NOT ELIGIBLE`(serviceStatus: ServiceStatus) {
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
  @EnumSource(value = ServiceStatus::class, names = ["NOT_STARTED", "NOT_ELIGIBLE"])
  fun `does not surface the CRS referral data when the CRS result is NOT STARTED or NOT ELIGIBLE`(serviceStatus: ServiceStatus) {
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
}
