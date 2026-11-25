package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityV2Dto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PpcServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import java.time.LocalDate
import java.util.UUID

fun buildEligibilityV2Dto(
  crn: String,
  cas1: Cas1ServiceResultWrapper = buildCas1ServiceResultV2(actionPosition = -1),
  cas2: Cas2ServiceResultWrapper = buildCas2ServiceResultV2(actionPosition = -1),
  cas3: Cas3ServiceResultWrapper = buildCas3ServiceResultV2(actionPosition = -1),
  dtr: DtrServiceResultWrapper = buildDtrServiceResultV2(actionPosition = -1),
  crs: CrsServiceResultWrapper = buildCrsServiceResultV2(actionPosition = -1),
  pa: PaServiceResultWrapper = buildPaServiceResultV2(actionPosition = -1),
  ppc: PpcServiceResultWrapper = buildPpcServiceResultV2(actionPosition = -1),
) = EligibilityV2Dto(
  crn,
  cas1,
  cas2,
  cas3,
  dtr,
  crs,
  pa,
  ppc,
)

fun buildServiceResultV2(
  serviceStatus: ServiceStatusV2 = ServiceStatusV2.CAS1_NOT_ELIGIBLE,
  url: String? = null,
  failureReasons: List<FailureReason> = emptyList(),
  actionStartDate: LocalDate? = null,
) = ServiceResultV2(
  serviceStatus = serviceStatus,
  url = url,
  failureReasons = failureReasons,
  actionStartDate = actionStartDate,
)

fun buildCas1ServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.CAS1_NOT_ELIGIBLE),
  cas1Application: Cas1ApplicationDto? = null,
  actionPosition: Int,
) = Cas1ServiceResultWrapper(
  serviceResult = serviceResult,
  cas1Application = cas1Application,
  actionPosition = actionPosition,
)

fun buildCas2ServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.CAS2_NOT_ELIGIBLE),
  cas2Application: Cas2ApplicationDto? = null,
  actionPosition: Int,
) = Cas2ServiceResultWrapper(
  serviceResult = serviceResult,
  cas2Application = cas2Application,
  actionPosition = actionPosition,
)

fun buildCas3ServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.CAS3_NOT_ELIGIBLE),
  cas3Application: Cas3ApplicationDto? = null,
  actionPosition: Int,
) = Cas3ServiceResultWrapper(
  serviceResult = serviceResult,
  cas3Application = cas3Application,
  actionPosition = actionPosition,
)

fun buildDtrServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.DTR_NOT_REQUIRED),
  caseId: UUID? = null,
  submission: DtrSubmissionDto? = null,
  actionPosition: Int,
) = DtrServiceResultWrapper(
  serviceResult = serviceResult,
  caseId = caseId,
  submission = submission,
  actionPosition = actionPosition,
)

fun buildCrsServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.CRS_NOT_REQUIRED),
  commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto? = null,
  actionPosition: Int,
) = CrsServiceResultWrapper(
  serviceResult = serviceResult,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  actionPosition = actionPosition,
)

fun buildPaServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.PA_NOT_ELIGIBLE),
  actionPosition: Int = -1,
) = PaServiceResultWrapper(
  serviceResult = serviceResult,
  actionPosition = actionPosition,
)

fun buildPpcServiceResultV2(
  serviceResult: ServiceResultV2 = buildServiceResultV2(ServiceStatusV2.PPC_NOT_REQUIRED),
  actionPosition: Int = -1,
) = PpcServiceResultWrapper(
  serviceResult = serviceResult,
  actionPosition = actionPosition,
)
