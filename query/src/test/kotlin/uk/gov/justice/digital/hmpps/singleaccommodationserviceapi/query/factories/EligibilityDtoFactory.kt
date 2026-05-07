package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import java.util.UUID

fun buildEligibilityDto(
  crn: String,
  cas1: Cas1ServiceResult = buildCas1ServiceResult(),
  cas3: Cas3ServiceResult = buildCas3ServiceResult(),
  caseActions: List<String> = emptyList(),
  dtr: DtrServiceResult = buildDtrServiceResult(),
  crs: CrsServiceResult = buildCrsServiceResult(),
  pa: PaServiceResult = buildPaServiceResult(),
) = EligibilityDto(
  crn,
  cas1,
  cas3,
  dtr,
  crs,
  pa,
  caseActions,
)

fun buildServiceResult(
  serviceStatus: ServiceStatus = ServiceStatus.NOT_ELIGIBLE,
  action: String? = null,
  link: String? = null,
) = ServiceResult(
  serviceStatus = serviceStatus,
  action = action,
  link = link,
)

fun buildCas1ServiceResult(
  result: ServiceResult = buildServiceResult(),
  cas1Application: Cas1ApplicationDto? = null,
) = Cas1ServiceResult(
  serviceResult = result,
  cas1Application = cas1Application,
)

fun buildCas3ServiceResult(
  result: ServiceResult = buildServiceResult(),
  cas3Application: Cas3ApplicationDto? = null,
) = Cas3ServiceResult(
  serviceResult = result,
  cas3Application = cas3Application,
)

fun buildDtrServiceResult(
  result: ServiceResult = buildServiceResult(),
  caseId: UUID? = null,
  submission: DtrSubmissionDto? = null,
) = DtrServiceResult(
  serviceResult = result,
  caseId = caseId,
  submission = submission,
)

fun buildCrsServiceResult(
  result: ServiceResult = buildServiceResult(),
  commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto? = null,
) = CrsServiceResult(
  serviceResult = result,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
)

fun buildPaServiceResult(
  result: ServiceResult = buildServiceResult(),
) = PaServiceResult(
  serviceResult = result,
)
