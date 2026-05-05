package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

data class CommissionedRehabilitativeServicesDto(
  val status: CrsStatus,
  val submissionDate: LocalDate,
)

enum class CrsStatus {
  NSI_REFERRAL,
  IN_PROGRESS,
  NSI_COMMENCED,
  APPOINTMENT,
  ACTION_PLAN_SUBMITTED,
  ACTION_PLAN_APPROVED,
  END_OF_SERVICE_REPORT,
  COMPLETED,
  NSI_TERMINATED,
}
