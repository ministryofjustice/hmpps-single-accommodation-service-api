package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

object EligibilityTransformer {
  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResult,
    cas3: ServiceResult,
    dtr: ServiceResult,
    dutyToRefer: DutyToReferDto?,
  ) = EligibilityDto(
    crn = crn,
    cas1 = cas1,
    cas3 = cas3,
    dtr = dtr,
    caseActions =
    listOf(
      dtr.action,
      cas1.action,
      cas3.action,
    ).mapNotNull { it },
    dutyToRefer = dutyToRefer,
  )
}
