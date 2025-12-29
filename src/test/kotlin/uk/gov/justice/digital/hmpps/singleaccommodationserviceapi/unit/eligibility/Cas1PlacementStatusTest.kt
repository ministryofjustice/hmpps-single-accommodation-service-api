package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import java.util.stream.Stream

@ExtendWith(value = [MockKExtension::class])
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
    @MockK
    lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

    @MockK
    lateinit var cas1Rules: Cas1RuleSet

    @MockK
    lateinit var rulesEngine: RulesEngine

    @InjectMockKs
    lateinit var eligibilityService: EligibilityService

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.Cas1PlacementStatusTest#provideStatusInputsAndOutputs")
    fun `transforms placementStatus to serviceStatus`(
      placementStatus: Cas1PlacementStatus,
      serviceStatus: ServiceStatus,
    ) {
      val result = with(eligibilityService) {
        placementStatus.toServiceStatus()
      }
      assertThat(result).isEqualTo(serviceStatus)
    }
  }
}
