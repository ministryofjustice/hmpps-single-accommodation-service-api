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
      "SUBMITTED,SUBMITTED",
      "IN_PROGRESS,SUBMITTED",
      "PENDING,SUBMITTED",
      "REQUESTED_FURTHER_INFORMATION,SUBMITTED",
      "AWAITING_PLACEMENT,CONFIRMED",
      "PLACED,CONFIRMED",
      "INAPPLICABLE,NOT_STARTED",
      "REJECTED,REJECTED",
      "WITHDRAWN,WITHDRAWN",
      "NULL,NOT_STARTED",
    ],
    nullValues = ["NULL"],
  )
  fun `transform to serviceStatus`(
    applicationStatus: Cas3ApplicationStatus?,
    serviceStatus: ServiceStatus,
  ) {
    assertThat(Cas3ServiceStatusTransformer.toServiceStatus(applicationStatus)).isEqualTo(serviceStatus)
  }
}
