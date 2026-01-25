package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ServiceStatusTransformer
import java.util.UUID

class ServiceStatusTransformerTest {

  private companion object {

    @JvmStatic
    fun provideCas1ApplicationStatusInputsAndOutputs(): Stream<Arguments?> =
      Stream.of(
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

  @Nested
  inner class Cas1ApplicationToServeStatusStatus {

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ServiceStatusTransformerTest#provideCas1ApplicationStatusInputsAndOutputs")
    fun `transform to serviceStatus`(
      applicationStatus: Cas1ApplicationStatus?,
      hasImminentActions: Boolean,
      serviceStatus: ServiceStatus,
    ) {
      assertThat(ServiceStatusTransformer.toServiceStatus(applicationStatus, hasImminentActions)).isEqualTo(serviceStatus)
    }

  }

  @Nested
  inner class Cas2HdcApplicationToServeStatusStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2HdcApplication = Cas2HdcApplication(
        id = UUID.randomUUID(),
      )
      assertThat(ServiceStatusTransformer.toServiceStatus(cas2HdcApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }

  @Nested
  inner class Cas2CourtBailApplicationToServeStatusStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2CourtBailApplication = Cas2CourtBailApplication(
        id = UUID.randomUUID(),
      )
      assertThat(ServiceStatusTransformer.toServiceStatus(cas2CourtBailApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }

  @Nested
  inner class Cas2PrisonBailApplicationToServeStatusStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2PrisonBailApplication = Cas2PrisonBailApplication(
        id = UUID.randomUUID(),
      )
      assertThat(ServiceStatusTransformer.toServiceStatus(cas2PrisonBailApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }
}
