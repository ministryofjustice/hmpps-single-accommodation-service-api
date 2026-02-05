package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ServiceStatusTransformer

class Cas3ServiceStatusTransformerTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "SUBMITTED,false,SUBMITTED",
      "IN_PROGRESS,false,SUBMITTED",
      "PENDING,false,SUBMITTED",
      "REQUESTED_FURTHER_INFORMATION,true,SUBMITTED",
      "AWAITING_PLACEMENT,true,CONFIRMED",
      "PLACED,true,CONFIRMED",
      "INAPPLICABLE,true,UPCOMING",
      "INAPPLICABLE,false,NOT_STARTED",
      "REJECTED,true,REJECTED",
      "WITHDRAWN,true,WITHDRAWN",
      "NULL,true,UPCOMING",
      "NULL,false,NOT_STARTED",
    ],
    nullValues = ["NULL"],
  )
  fun `transform to serviceStatus`(
    applicationStatus: Cas3ApplicationStatus?,
    isUpcoming: Boolean,
    serviceStatus: ServiceStatus,
  ) {
    assertThat(Cas3ServiceStatusTransformer.toServiceStatus(applicationStatus, isUpcoming)).isEqualTo(serviceStatus)
  }
}
