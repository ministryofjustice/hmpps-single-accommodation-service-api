package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ServiceStatusTransformer
import java.util.UUID
import java.util.stream.Stream

class Cas1ServiceStatusTransformerTest {

  private companion object {

    val id = UUID.randomUUID()

    @JvmStatic
    fun provideCas1ApplicationStatusInputsAndOutputs(): Stream<Arguments?> =
      Stream.of(
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.AWAITING_ASSESSMENT,
            null,
            null,
          ),
          true,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
            null,
            null,
          ),
          true,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
            null,
            null,
          ),
          true,
          ServiceStatus.SUBMITTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
            null,
            null,
          ),
          true,
          ServiceStatus.INFO_REQUESTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
            null,
            null,
          ),
          true,
          ServiceStatus.PLACEMENT_BOOKED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.AWAITING_PLACEMENT,
            null,
            null,
          ),
          true,
          ServiceStatus.PLACEMENT_BOOKED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            Cas1PlacementStatus.ARRIVED,
            null,
          ),
          true,
          ServiceStatus.PLACEMENT_BOOKED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            Cas1PlacementStatus.UPCOMING,
            null,
          ),
          true,
          ServiceStatus.PLACEMENT_BOOKED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            Cas1PlacementStatus.NOT_ARRIVED,
            null,
          ),
          true,
          ServiceStatus.NOT_ARRIVED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.STARTED,
            null,
            null,
          ),
          true,
          ServiceStatus.NOT_SUBMITTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.REJECTED,
            null,
            null,
          ),
          true,
          ServiceStatus.REJECTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.INAPPLICABLE,
            null,
            null,
          ),
          true,
          ServiceStatus.NOT_STARTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.INAPPLICABLE,
            null,
            null,
          ),
          false,
          ServiceStatus.UPCOMING,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.WITHDRAWN,
            null,
            null,
          ),
          true,
          ServiceStatus.NOT_STARTED,
        ),
        Arguments.of(
          Cas1Application(
            id,
            Cas1ApplicationStatus.EXPIRED,
            null,
            null,
          ),
          true,
          ServiceStatus.NOT_STARTED,
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
    application: Cas1Application?,
    isWithinOneYear: Boolean,
    serviceStatus: ServiceStatus,
  ) {
    assertThat(Cas1ServiceStatusTransformer.toServiceStatus(application, isWithinOneYear)).isEqualTo(serviceStatus)
  }
}
