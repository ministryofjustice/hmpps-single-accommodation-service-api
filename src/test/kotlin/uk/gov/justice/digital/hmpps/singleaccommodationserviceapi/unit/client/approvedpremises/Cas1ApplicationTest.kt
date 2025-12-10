package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.UUID
import java.util.stream.Stream

class Cas1ApplicationTest {
  private companion object {
    private const val CREATE_PLACEMENT_REQUEST_ACTION = "Create a placement request."

    @JvmStatic
    fun provideStatusInputsAndOutputActions(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
        null,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        Cas1PlacementStatus.ARRIVED,
        listOf<String>(),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        Cas1PlacementStatus.UPCOMING,
        listOf<String>(),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        Cas1PlacementStatus.DEPARTED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        Cas1PlacementStatus.NOT_ARRIVED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        Cas1PlacementStatus.CANCELLED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
    )

    @JvmStatic
    fun provideApplicationStatusInputsAndServiceStatusOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        ServiceStatus.AWAITING_ASSESSMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
        ServiceStatus.ASSESSMENT_IN_PROGRESS,
      ),
      Arguments.of(
        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        ServiceStatus.UNALLOCATED_ASSESSMENT,
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
        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        ServiceStatus.AWAITING_PLACEMENT,
      ),
    )

    @JvmStatic
    fun providePlacementStatusInputsAndServiceStatusOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1PlacementStatus.UPCOMING,
        ServiceStatus.UPCOMING_PLACEMENT,
      ),
      Arguments.of(
        Cas1PlacementStatus.NOT_ARRIVED,
        ServiceStatus.NOT_ARRIVED,
      ),
      Arguments.of(
        Cas1PlacementStatus.DEPARTED,
        ServiceStatus.DEPARTED,
      ),
      Arguments.of(
        Cas1PlacementStatus.ARRIVED,
        ServiceStatus.ARRIVED,
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
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.Cas1ApplicationTest#providePlacementStatusInputsAndServiceStatusOutputs")
    fun `cas 1 application present with status PLACEMENT_ALLOCATED so result contains appropriate placement status`(
      placementStatus: Cas1PlacementStatus,
      serviceStatus: ServiceStatus,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = placementStatus,
      )
      val result = cas1Application.toServiceStatus()
      assertThat(result).isEqualTo(serviceStatus)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.Cas1ApplicationTest#provideApplicationStatusInputsAndServiceStatusOutputs")
    fun `cas 1 application present with status not PLACEMENT_ALLOCATED so result contains appropriate application status`(
      applicationStatus: Cas1ApplicationStatus,
      serviceStatus: ServiceStatus,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = applicationStatus,
        placementStatus = null,
      )
      val result = cas1Application.toServiceStatus()
      assertThat(result).isEqualTo(serviceStatus)
    }

    @Test
    fun `errors as status PLACEMENT_ALLOCATED but no placement status`() {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = null,
      )
      assertThatThrownBy { cas1Application.toServiceStatus() }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("Null Placement Status")
    }
  }

  @Nested
  inner class BuildActions {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.Cas1ApplicationTest#provideStatusInputsAndOutputActions")
    fun `cas 1 application present so result contains appropriate actions`(
      applicationStatus: Cas1ApplicationStatus,
      placementStatus: Cas1PlacementStatus?,
      actions: List<String>,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = applicationStatus,
        placementStatus = placementStatus,
      )
      val result = cas1Application.buildActions()
      assertThat(result).isEqualTo(actions)
    }
  }
}
