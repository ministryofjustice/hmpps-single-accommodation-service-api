package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus

object Cas3ServiceStatusTransformer {
  fun toServiceStatus(cas3ApplicationStatus: Cas3ApplicationStatus?, isUpcoming: Boolean) =
    when (cas3ApplicationStatus) {
      Cas3ApplicationStatus.PLACED,
      Cas3ApplicationStatus.AWAITING_PLACEMENT,
        -> ServiceStatus.CONFIRMED

      Cas3ApplicationStatus.SUBMITTED,
      Cas3ApplicationStatus.IN_PROGRESS,
      Cas3ApplicationStatus.PENDING,
      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION
        -> ServiceStatus.SUBMITTED

      null,
      Cas3ApplicationStatus.INAPPLICABLE
        ->
        when (isUpcoming) {
          true -> ServiceStatus.UPCOMING
          false -> ServiceStatus.NOT_STARTED
        }

      Cas3ApplicationStatus.REJECTED
        -> ServiceStatus.REJECTED

      Cas3ApplicationStatus.WITHDRAWN,
        -> ServiceStatus.WITHDRAWN
    }
}
