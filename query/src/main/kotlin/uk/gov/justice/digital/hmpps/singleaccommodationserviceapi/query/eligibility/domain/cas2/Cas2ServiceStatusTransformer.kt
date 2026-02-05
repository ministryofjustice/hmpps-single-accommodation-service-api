package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication

object Cas2ServiceStatusTransformer {
  fun toServiceStatus(cas2HdcApplication: Cas2HdcApplication) = ServiceStatus.NOT_STARTED

  fun toServiceStatus(cas2CourtBailApplication: Cas2CourtBailApplication) = ServiceStatus.NOT_STARTED

  fun toServiceStatus(cas2PrisonBailApplication: Cas2PrisonBailApplication) = ServiceStatus.NOT_STARTED
}
