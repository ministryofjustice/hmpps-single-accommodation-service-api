package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsStatus
import java.time.LocalDate

fun buildCommissionedRehabilitativeServicesDto(
  status: CrsStatus = CrsStatus.COMPLETED,
  submissionDate: LocalDate = LocalDate.now(),
) = CommissionedRehabilitativeServicesDto(
  status = status,
  submissionDate = submissionDate,
)
