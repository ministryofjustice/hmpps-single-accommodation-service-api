package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus

object Cas3ServiceStatusTransformer {
  fun toServiceStatus(cas3ApplicationStatus: Cas3ApplicationStatus?) = when (cas3ApplicationStatus) {
    Cas3ApplicationStatus.SUBMITTED,
    Cas3ApplicationStatus.IN_PROGRESS,
    Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
    -> ServiceStatus.SUBMITTED

    null,
    -> ServiceStatus.NOT_STARTED

    Cas3ApplicationStatus.REJECTED,
    -> ServiceStatus.REJECTED
  }
}
