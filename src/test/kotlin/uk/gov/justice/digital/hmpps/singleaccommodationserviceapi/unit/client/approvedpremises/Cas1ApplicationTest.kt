package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
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
        Cas1ApplicationStatus.AwaitingAssesment,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.UnallocatedAssesment,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.AssesmentInProgress,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.AwaitingPlacement,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.RequestedFurtherInformation,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PendingPlacementRequest,
        Cas1PlacementStatus.NOT_ALLOCATED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.ARRIVED,
        listOf<String>(),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.UPCOMING,
        listOf<String>(),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.DEPARTED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.NOT_ARRIVED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.CANCELLED,
        listOf(CREATE_PLACEMENT_REQUEST_ACTION),
      ),
    )

    @JvmStatic
    fun provideStatusInputsAndOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        Cas1ApplicationStatus.AwaitingAssesment,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.AWAITING_ASSESSMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.UnallocatedAssesment,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.UNALLOCATED_ASSESSMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.AssesmentInProgress,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.ASSESSMENT_IN_PROGRESS,
      ),
      Arguments.of(
        Cas1ApplicationStatus.AwaitingPlacement,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.AWAITING_PLACEMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.RequestedFurtherInformation,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PendingPlacementRequest,
        Cas1PlacementStatus.NOT_ALLOCATED,
        ServiceStatus.PENDING_PLACEMENT_REQUEST,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.ARRIVED,
        ServiceStatus.ARRIVED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.UPCOMING,
        ServiceStatus.UPCOMING_PLACEMENT,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.DEPARTED,
        ServiceStatus.DEPARTED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.NOT_ARRIVED,
        ServiceStatus.NOT_ARRIVED,
      ),
      Arguments.of(
        Cas1ApplicationStatus.PlacementAllocated,
        Cas1PlacementStatus.CANCELLED,
        ServiceStatus.CANCELLED,
      ),
    )
  }

  @Nested
  inner class TransformToServiceStatus {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.Cas1ApplicationTest#provideStatusInputsAndOutputs")
    fun `cas 1 application present so result contains appropriate status`(
      applicationStatus: Cas1ApplicationStatus,
      placementStatus: Cas1PlacementStatus,
      serviceStatus: ServiceStatus,
    ) {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = applicationStatus,
        placementStatus = placementStatus,
      )
      val result = cas1Application.transformToServiceStatus()
      assertThat(result).isEqualTo(serviceStatus)
    }
  }

  @Nested
  inner class BuildActions {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises.Cas1ApplicationTest#provideStatusInputsAndOutputActions")
    fun `cas 1 application present so result contains appropriate actions`(
      applicationStatus: Cas1ApplicationStatus,
      placementStatus: Cas1PlacementStatus,
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
