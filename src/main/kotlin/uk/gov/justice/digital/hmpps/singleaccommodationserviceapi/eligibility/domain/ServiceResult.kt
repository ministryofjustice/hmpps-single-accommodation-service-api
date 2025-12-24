package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val suitableApplication: SuitableApplication? = null,
  val actions: List<String>,
) {
  fun getCaseStatus() = when (serviceStatus) {
    ServiceStatus.UPCOMING -> CaseStatus.ACTION_UPCOMING
    ServiceStatus.NOT_ELIGIBLE, ServiceStatus.ARRIVED, ServiceStatus.UPCOMING_PLACEMENT -> CaseStatus.NO_ACTION_NEEDED
    else -> CaseStatus.ACTION_NEEDED
  }
}
