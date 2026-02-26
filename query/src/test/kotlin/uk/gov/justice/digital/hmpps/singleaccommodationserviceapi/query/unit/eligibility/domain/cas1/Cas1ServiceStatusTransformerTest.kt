package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ServiceStatusTransformer
import java.util.stream.Stream

class Cas1ServiceStatusTransformerTest {

  private companion object {

    @JvmStatic
    fun provideCas1ApplicationStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        false,
        ServiceStatus.SUBMITTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        false,
        ServiceStatus.SUBMITTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
        false,
        ServiceStatus.SUBMITTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        true,
        ServiceStatus.SUBMITTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
        true,
        ServiceStatus.CONFIRMED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        true,
        ServiceStatus.CONFIRMED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        true,
        ServiceStatus.CONFIRMED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.STARTED,
        false,
        ServiceStatus.UPCOMING,
      ),
      Arguments.of(
        Cas1ApplicationStatus.STARTED,
        true,
        ServiceStatus.NOT_STARTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.REJECTED,
        true,
        ServiceStatus.REJECTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.INAPPLICABLE,
        true,
        ServiceStatus.NOT_STARTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.INAPPLICABLE,
        false,
        ServiceStatus.UPCOMING,
      ),
      Arguments.of(
        Cas1ApplicationStatus.WITHDRAWN,
        true,
        ServiceStatus.WITHDRAWN,
      ),
      Arguments.of(
        Cas1ApplicationStatus.EXPIRED,
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
  @MethodSource("provideCas1ApplicationStatusInputsAndOutputs")
  fun `transform to serviceStatus`(
    applicationStatus: Cas1ApplicationStatus?,
    hasImminentActions: Boolean,
    serviceStatus: ServiceStatus,
  ) {
    assertThat(Cas1ServiceStatusTransformer.toServiceStatus(applicationStatus, hasImminentActions)).isEqualTo(serviceStatus)
  }
}
