package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

enum class Cas1PlacementStatus {
  ARRIVED,
  UPCOMING,
  DEPARTED,
  NOT_ARRIVED,
  CANCELLED,
  ;

  fun toServiceStatus() = when (this) {
    UPCOMING -> ServiceStatus.UPCOMING_PLACEMENT
    ARRIVED -> ServiceStatus.ARRIVED
    DEPARTED -> ServiceStatus.DEPARTED
    NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    CANCELLED -> ServiceStatus.CANCELLED
  }
}
