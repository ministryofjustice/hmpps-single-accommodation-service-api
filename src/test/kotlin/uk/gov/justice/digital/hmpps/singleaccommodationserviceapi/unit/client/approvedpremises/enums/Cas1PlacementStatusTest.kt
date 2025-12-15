package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.enums

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.stream.Stream

class Cas1PlacementStatusTest {
  private companion object {

    @JvmStatic
    fun provideStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1PlacementStatus.ARRIVED,
        ServiceStatus.ARRIVED,
      ),
      Arguments.of(
        Cas1PlacementStatus.UPCOMING,
        ServiceStatus.UPCOMING_PLACEMENT,
      ),
      Arguments.of(
        Cas1PlacementStatus.DEPARTED,
        ServiceStatus.DEPARTED,
      ),
      Arguments.of(
        Cas1PlacementStatus.NOT_ARRIVED,
        ServiceStatus.NOT_ARRIVED,
      ),
      Arguments.of(
        Cas1PlacementStatus.CANCELLED,
        ServiceStatus.CANCELLED,
      ),
    )
  }

  @Nested
  inner class TransformToServiceStatus {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.enums.Cas1PlacementStatusTest#provideStatusInputsAndOutputs")
    fun `transforms placementStatus to serviceStatus`(
      placementStatus: Cas1PlacementStatus,
      serviceStatus: ServiceStatus,
    ) {
      val result = placementStatus.toServiceStatus()
      assertThat(result).isEqualTo(serviceStatus)
    }
  }
}
