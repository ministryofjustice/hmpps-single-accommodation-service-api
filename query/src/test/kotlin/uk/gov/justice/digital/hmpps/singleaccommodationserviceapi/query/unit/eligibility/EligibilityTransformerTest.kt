package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_APPROVED_PREMISE_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.WAIT_FOR_ASSESSMENT_RESULT
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
    caseActions: List<String>,
  ) {
    val actualEligibility = EligibilityTransformer.toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas3 = cas3,
    )

    val expectedEligibility = buildEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas3 = cas3,
      caseActions = caseActions,
    )

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  private companion object {
    private const val CRN = "FAKECRN1"
    private val notStarted = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplicationId = null,
      action = START_APPROVED_PREMISE_APPLICATION,
    )
    private val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplicationId = null,
    )
    private val upcoming = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      suitableApplicationId = null,
      action = "$START_APPROVED_PREMISE_APPLICATION in 2 days",
    )
    private val confirmed = ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
      suitableApplicationId = null,
    )
    private val assessing = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = WAIT_FOR_ASSESSMENT_RESULT,
    )
    private val submitted = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = CREATE_PLACEMENT,
    )
    private val withdrawn = ServiceResult(
      serviceStatus = ServiceStatus.WITHDRAWN,
      suitableApplicationId = null,
      action = START_APPROVED_PREMISE_APPLICATION,
    )
    private val rejected = ServiceResult(
      serviceStatus = ServiceStatus.APPLICATION_REJECTED,
      suitableApplicationId = null,
      action = START_APPROVED_PREMISE_APPLICATION,
    )

    @JvmStatic
    fun provideEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        CRN,
        notEligible,
        notEligible,
        listOf<String>(),
      ),
      Arguments.of(
        CRN,
        notStarted,
        notEligible,
        listOf(START_APPROVED_PREMISE_APPLICATION),
      ),
      Arguments.of(
        CRN,
        upcoming,
        notEligible,
        listOf("$START_APPROVED_PREMISE_APPLICATION in 2 days"),
      ),
      Arguments.of(
        CRN,
        confirmed,
        notEligible,
        listOf<String>(),
      ),
      Arguments.of(
        CRN,
        assessing,
        notEligible,
        listOf(WAIT_FOR_ASSESSMENT_RESULT),
      ),
      Arguments.of(
        CRN,
        submitted,
        notEligible,
        listOf(CREATE_PLACEMENT),
      ),
      Arguments.of(
        CRN,
        withdrawn,
        notEligible,
        listOf(START_APPROVED_PREMISE_APPLICATION),
      ),
      Arguments.of(
        CRN,
        rejected,
        upcoming,
        listOf(
          START_APPROVED_PREMISE_APPLICATION,
          "$START_APPROVED_PREMISE_APPLICATION in 2 days",
        ),
      ),
    )
  }
}
