package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus

fun buildEligibilityDto(
  crn: String,
  cas1: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  cas3: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  caseActions: List<String> = emptyList(),
  dtr: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  dutyToRefer: DutyToReferDto? = null,
) = EligibilityDto(
  crn,
  cas1,
  cas3,
  dtr,
  caseActions,
  dutyToRefer,
)
