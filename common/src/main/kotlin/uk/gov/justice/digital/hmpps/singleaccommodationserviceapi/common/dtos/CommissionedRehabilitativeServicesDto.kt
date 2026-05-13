package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

data class CommissionedRehabilitativeServicesDto(
  val status: CrsStatus,
  val submissionDate: LocalDate,
)

enum class CrsStatus {
  DRAFT,
  LIVE,
  COMPLETED,
  WITHDRAWN,
}
