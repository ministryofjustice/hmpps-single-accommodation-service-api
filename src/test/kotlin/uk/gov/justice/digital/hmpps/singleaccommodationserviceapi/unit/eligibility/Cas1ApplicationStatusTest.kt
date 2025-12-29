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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import java.util.stream.Stream

@ExtendWith(value = [MockKExtension::class])
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
    @MockK
    lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

    @MockK
    lateinit var cas1Rules: Cas1RuleSet

    @MockK
    lateinit var rulesEngine: RulesEngine

    @InjectMockKs
    lateinit var eligibilityService: EligibilityService

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.Cas1ApplicationStatusTest#provideStatusInputsAndOutputs")
    fun `transforms applicationStatus to serviceStatus`(
      applicationStatus: Cas1ApplicationStatus,
      serviceStatus: ServiceStatus?,
    ) {
      val result = with(eligibilityService) {
        applicationStatus.toServiceStatus()
      }
      assertThat(result).isEqualTo(serviceStatus)
    }
  }
}
