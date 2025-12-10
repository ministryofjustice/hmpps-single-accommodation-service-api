package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.enums

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.stream.Stream

class Cas1ApplicationStatusTest {
  private companion object {
    @JvmStatic
    fun provideStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        ServiceStatus.AWAITING_ASSESSMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        ServiceStatus.UNALLOCATED_ASSESSMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
        ServiceStatus.ASSESSMENT_IN_PROGRESS,
      ),
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        ServiceStatus.AWAITING_PLACEMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
        ServiceStatus.PENDING_PLACEMENT_REQUEST,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        null,
      ),
    )
  }

  @Nested
  inner class ToServiceStatus {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.enums.Cas1ApplicationStatusTest#provideStatusInputsAndOutputs")
    fun `transforms applicationStatus to serviceStatus`(
      applicationStatus: Cas1ApplicationStatus,
      serviceStatus: ServiceStatus?,
    ) {
      val result = applicationStatus.toServiceStatus()
      assertThat(result).isEqualTo(serviceStatus)
    }
  }
}
