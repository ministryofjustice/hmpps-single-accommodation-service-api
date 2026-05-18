package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
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
    val cas1 = buildServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      action = EligibilityKeys.PROVIDE_INFORMATION,
      link = EligibilityKeys.VIEW_APPLICATION,
    )
    val cas3 = buildServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      link = EligibilityKeys.VIEW_REFERRAL,
    )
    val dtr = buildServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      action = EligibilityKeys.ADD_DTR_OUTCOME,
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
    val caseActions = listOfNotNull(dtr.action, crs.action, cas1.action, cas3.action, pa.action)

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
