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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import java.time.LocalDate
import java.util.UUID

fun buildEligibilityV2Dto(
  crn: String,
  cas1: Cas1ServiceResultWrapper = buildCas1ServiceResultNew(actionPosition = -1),
  cas2: Cas2ServiceResultWrapper = buildCas2ServiceResultNew(actionPosition = -1),
  cas3: Cas3ServiceResultWrapper = buildCas3ServiceResultNew(actionPosition = -1),
  dtr: DtrServiceResultWrapper = buildDtrServiceResultNew(actionPosition = -1),
  crs: CrsServiceResultWrapper = buildCrsServiceResultNew(actionPosition = -1),
  pa: PaServiceResultWrapper = buildPaServiceResultNew(actionPosition = -1),
) = EligibilityV2Dto(
  crn,
  cas1,
  cas2,
  cas3,
  dtr,
  crs,
  pa,
)

fun buildServiceResultNew(
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

fun buildCas1ServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.CAS1_NOT_ELIGIBLE),
  cas1Application: Cas1ApplicationDto? = null,
  actionPosition: Int,
) = Cas1ServiceResultWrapper(
  serviceResult = serviceResult,
  cas1Application = cas1Application,
  actionPosition = actionPosition,
)

fun buildCas2ServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.CAS2_NOT_ELIGIBLE),
  cas2Application: Cas2ApplicationDto? = null,
  actionPosition: Int,
) = Cas2ServiceResultWrapper(
  serviceResult = serviceResult,
  cas2Application = cas2Application,
  actionPosition = actionPosition,
)

fun buildCas3ServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.CAS3_NOT_ELIGIBLE),
  cas3Application: Cas3ApplicationDto? = null,
  actionPosition: Int,
) = Cas3ServiceResultWrapper(
  serviceResult = serviceResult,
  cas3Application = cas3Application,
  actionPosition = actionPosition,
)

fun buildDtrServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.DTR_NOT_ELIGIBLE),
  caseId: UUID? = null,
  submission: DtrSubmissionDto? = null,
  actionPosition: Int,
) = DtrServiceResultWrapper(
  serviceResult = serviceResult,
  caseId = caseId,
  submission = submission,
  actionPosition = actionPosition,
)

fun buildCrsServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.CRS_NOT_ELIGIBLE),
  commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto? = null,
  actionPosition: Int,
) = CrsServiceResultWrapper(
  serviceResult = serviceResult,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  actionPosition = actionPosition,
)

fun buildPaServiceResultNew(
  serviceResult: ServiceResultV2 = buildServiceResultNew(ServiceStatusV2.PA_NOT_ELIGIBLE),
  actionPosition: Int = -1,
) = PaServiceResultWrapper(
  serviceResult = serviceResult,
  actionPosition = actionPosition,
)
