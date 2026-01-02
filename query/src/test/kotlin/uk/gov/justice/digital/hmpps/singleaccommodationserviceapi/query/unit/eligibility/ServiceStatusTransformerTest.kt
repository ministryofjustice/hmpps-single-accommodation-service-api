package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.toServiceStatus
import java.util.UUID
import java.util.stream.Stream

class ServiceStatusTransformerTest {
  private companion object {
    @JvmStatic
    fun provideCas1ApplicationStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
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
    )

    @JvmStatic
    fun provideCas1PlacementStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
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
  inner class TransformCas1ApplicationToServeStatusStatus {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ServiceStatusTransformerTest#provideCas1ApplicationStatusInputsAndOutputs")
    fun `transform to service-status where application-status provided and is not PLACEMENT_ALLOCATED`(
      applicationStatus: Cas1ApplicationStatus,
      serviceStatus: ServiceStatus,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = applicationStatus,
        placementStatus = null,
      )
      assertThat(toServiceStatus(cas1Application)).isEqualTo(serviceStatus)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ServiceStatusTransformerTest#provideCas1PlacementStatusInputsAndOutputs")
    fun `transform to service-status where application-status is PLACEMENT_ALLOCATED and placement status is set`(
      placementStatus: Cas1PlacementStatus,
      serviceStatus: ServiceStatus,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = placementStatus,
      )
      assertThat(toServiceStatus(cas1Application)).isEqualTo(serviceStatus)
    }

    @Test
    fun `errors as application-status is PLACEMENT_ALLOCATED but placement status is not set`() {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = null,
      )
      assertThatThrownBy { toServiceStatus(cas1Application) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("Null Placement Status")
    }
  }
}
