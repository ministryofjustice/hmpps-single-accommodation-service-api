package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsStatus
import java.time.LocalDate

fun buildCommissionedRehabilitativeServices(
  submissionDate: LocalDate = LocalDate.now(),
  status: CrsStatus = CrsStatus.COMPLETED,
) = CommissionedRehabilitativeServices(
  status = status,
  submissionDate = submissionDate,
)
