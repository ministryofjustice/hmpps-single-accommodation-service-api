package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val suitableApplication: SuitableApplication? = null,
  val actions: List<String>,
)
