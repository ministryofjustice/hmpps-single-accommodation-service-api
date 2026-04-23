package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.Clock

object CrsServiceResultTransformer {
  fun toCrsServiceResult(data: DomainData, clock: Clock): ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE)
}
