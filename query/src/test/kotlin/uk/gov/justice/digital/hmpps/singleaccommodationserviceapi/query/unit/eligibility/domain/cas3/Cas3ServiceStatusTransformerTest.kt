package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.toServiceStatus
import java.util.stream.Stream

class Cas3ServiceStatusTransformerTest {

  private companion object {

    @JvmStatic
    fun provideCas3ApplicationStatusInputsAndOutputs(): Stream<Arguments?> =
      Stream.of(
        Arguments.of(
          Cas3ApplicationStatus.SUBMITTED,
          false,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.IN_PROGRESS,
          false,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.PENDING,
          false,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
          true,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.AWAITING_PLACEMENT,
          true,
          ServiceStatus.CONFIRMED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.PLACED,
          true,
          ServiceStatus.CONFIRMED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.INAPPLICABLE,
          false,
          ServiceStatus.UPCOMING,
        ),
        Arguments.of(
          Cas3ApplicationStatus.INAPPLICABLE,
          true,
          ServiceStatus.NOT_STARTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.REJECTED,
          true,
          ServiceStatus.REJECTED,
        ),
        Arguments.of(
          Cas3ApplicationStatus.WITHDRAWN,
          true,
          ServiceStatus.WITHDRAWN,
        ),
        Arguments.of(
          null,
          false,
          ServiceStatus.UPCOMING,
        ),
        Arguments.of(
          null,
          true,
          ServiceStatus.NOT_STARTED,
        ),
      )
  }

  @ParameterizedTest
  @MethodSource("provideCas3ApplicationStatusInputsAndOutputs")
  fun `transform to serviceStatus`(
    applicationStatus: Cas3ApplicationStatus?,
    hasImminentActions: Boolean,
    serviceStatus: ServiceStatus,
  ) {
    assertThat(toServiceStatus(applicationStatus, hasImminentActions)).isEqualTo(serviceStatus)
  }
}
