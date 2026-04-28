package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import java.util.stream.Stream

class EligibilityTransformerTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.EligibilityTransformerTest#provideEligibility",
  )
  fun `should transform to eligibility`(
    crn: String,
    cas1: ServiceResult,
    cas3: ServiceResult,
    dtr: ServiceResult,
    caseActions: List<String>,
  ) {
    val actualEligibility = EligibilityTransformer.toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      dutyToRefer = null,
    )

    val expectedEligibility = buildEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      caseActions = caseActions,
    )

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  private companion object {
    private const val CRN = "FAKECRN1"
    private val notStarted = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplicationId = null,
      action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
    )
    private val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplicationId = null,
    )
    private val upcoming = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      suitableApplicationId = null,
      action = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days",
    )
    private val confirmed = ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
      suitableApplicationId = null,
    )
    private val assessing = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = EligibilityKeys.WAIT_FOR_ASSESSMENT_RESULT,
    )
    private val submitted = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = EligibilityKeys.CREATE_PLACEMENT,
    )
    private val withdrawn = ServiceResult(
      serviceStatus = ServiceStatus.WITHDRAWN,
      suitableApplicationId = null,
      action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
    )
    private val rejected = ServiceResult(
      serviceStatus = ServiceStatus.APPLICATION_REJECTED,
      suitableApplicationId = null,
      action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
    )

    @JvmStatic
    fun provideEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        CRN,
        notEligible,
        notEligible,
        notEligible,
        emptyList<String>(),
      ),
      Arguments.of(
        CRN,
        notStarted,
        notEligible,
        notEligible,
        listOf(EligibilityKeys.START_APPROVED_PREMISE_APPLICATION),
      ),
      Arguments.of(
        CRN,
        upcoming,
        notEligible,
        notEligible,
        listOf("${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days"),
      ),
      Arguments.of(
        CRN,
        confirmed,
        notEligible,
        notEligible,
        emptyList<String>(),
      ),
      Arguments.of(
        CRN,
        assessing,
        notEligible,
        notEligible,
        listOf(EligibilityKeys.WAIT_FOR_ASSESSMENT_RESULT),
      ),
      Arguments.of(
        CRN,
        submitted,
        notEligible,
        notEligible,
        listOf(EligibilityKeys.CREATE_PLACEMENT),
      ),
      Arguments.of(
        CRN,
        withdrawn,
        notEligible,
        notEligible,
        listOf(EligibilityKeys.START_APPROVED_PREMISE_APPLICATION),
      ),
      Arguments.of(
        CRN,
        rejected,
        upcoming,
        notEligible,
        listOf(
          EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
          "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days",
        ),
      ),
    )
  }
}
