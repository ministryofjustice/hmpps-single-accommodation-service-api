package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildActions
import java.util.UUID
import java.util.stream.Stream

class ActionTransformerTest {

  companion object {
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
  }

  @Nested
  inner class BuildActions {
    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ActionTransformerTest#provideStatusInputsAndOutputActions")
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
      val result = buildActions(cas1Application)
      Assertions.assertThat(result).isEqualTo(actions)
    }
  }
}