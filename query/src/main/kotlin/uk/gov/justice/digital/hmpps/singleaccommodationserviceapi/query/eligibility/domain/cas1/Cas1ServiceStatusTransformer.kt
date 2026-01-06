package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus

object ServiceStatusTransformer {
  fun toServiceStatus(cas1ApplicationStatus: Cas1ApplicationStatus?, hasImminentActions: Boolean) = when (cas1ApplicationStatus) {
    Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      -> ServiceStatus.CONFIRMED

    Cas1ApplicationStatus.AWAITING_PLACEMENT,
    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST
      -> ServiceStatus.CONFIRMED

    Cas1ApplicationStatus.AWAITING_ASSESSMENT,
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION
      -> ServiceStatus.SUBMITTED

    null,
    Cas1ApplicationStatus.STARTED,
    Cas1ApplicationStatus.INAPPLICABLE
      ->
      when (hasImminentActions) {
        true -> ServiceStatus.NOT_STARTED
        false -> ServiceStatus.UPCOMING
      }

    Cas1ApplicationStatus.REJECTED
      -> ServiceStatus.REJECTED

    Cas1ApplicationStatus.WITHDRAWN,
    Cas1ApplicationStatus.EXPIRED
      -> ServiceStatus.WITHDRAWN
  }
}
