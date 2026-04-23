package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices

import java.time.LocalDate

data class CommissionedRehabilitativeServices(
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
