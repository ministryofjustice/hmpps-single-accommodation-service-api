package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import java.util.UUID

fun buildEligibilityDtoNew(
  crn: String,
  cas1: Cas1ServiceResultNew = buildCas1ServiceResultNew(),
  cas2: Cas2ServiceResultNew = buildCas2ServiceResultNew(),
  cas3: Cas3ServiceResultNew = buildCas3ServiceResultNew(),
  caseActions: List<CaseAction> = emptyList(),
  dtr: DtrServiceResultNew = buildDtrServiceResultNew(),
  crs: CrsServiceResultNew = buildCrsServiceResultNew(),
  pa: PaServiceResultNew = buildPaServiceResultNew(),
) = EligibilityDtoNew(
  crn,
  cas1,
  cas2,
  cas3,
  dtr,
  crs,
  pa,
  caseActions,
)

fun buildServiceResultNew(
  serviceStatus: ServiceStatusNew = ServiceStatusNew.CAS1_NOT_ELIGIBLE,
  action: CaseAction? = null,
  link: String? = null,
  url: String? = null,
  linkType: LinkType? = null,
  failureReasons: List<FailureReason> = emptyList(),
) = ServiceResultNew(
  serviceStatus = serviceStatus,
  action = action,
  link = link,
  url = url,
  linkType = linkType,
  failureReasons = failureReasons,
)

fun buildCas1ServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.CAS1_NOT_ELIGIBLE),
  cas1Application: Cas1ApplicationDto? = null,
) = Cas1ServiceResultNew(
  serviceResult = serviceResult,
  cas1Application = cas1Application,
)

fun buildCas2ServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.CAS2_NOT_ELIGIBLE),
  cas2Application: Cas2ApplicationDto? = null,
) = Cas2ServiceResultNew(
  serviceResult = serviceResult,
  cas2Application = cas2Application,
)

fun buildCas3ServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.CAS3_NOT_ELIGIBLE),
  cas3Application: Cas3ApplicationDto? = null,
) = Cas3ServiceResultNew(
  serviceResult = serviceResult,
  cas3Application = cas3Application,
)

fun buildDtrServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.DTR_NOT_ELIGIBLE),
  caseId: UUID? = null,
  submission: DtrSubmissionDto? = null,
) = DtrServiceResultNew(
  serviceResult = serviceResult,
  caseId = caseId,
  submission = submission,
)

fun buildCrsServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.CRS_NOT_ELIGIBLE),
  commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto? = null,
) = CrsServiceResultNew(
  serviceResult = serviceResult,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
)

fun buildPaServiceResultNew(
  serviceResult: ServiceResultNew = buildServiceResultNew(ServiceStatusNew.PA_NOT_ELIGIBLE),
) = PaServiceResultNew(
  serviceResult = serviceResult,
)
