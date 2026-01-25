package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.AWAIT_ASSESSMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_APPROVED_PREMISE_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer
import java.util.stream.Stream

class EligibilityTransformerTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.EligibilityTransformerTest#provideEligibility",
  )
  fun `should transform to eligibility`(
    crn: String,
    cas1: ServiceResult,
    cas2Hdc: ServiceResult?,
    cas2PrisonBail: ServiceResult?,
    cas2CourtBail: ServiceResult?,
    cas3: ServiceResult?,
    caseStatus: CaseStatus,
    caseActions: List<String>,
  ) {

    val actualEligibility = EligibilityTransformer.toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2Hdc = cas2Hdc,
      cas2PrisonBail = cas2PrisonBail,
      cas2CourtBail = cas2CourtBail,
      cas3 = cas3,
    )

    val expectedEligibility = EligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2Hdc = cas2Hdc,
      cas2PrisonBail = cas2PrisonBail,
      cas2CourtBail = cas2CourtBail,
      cas3 = cas3,
      caseStatus = caseStatus,
      caseActions = caseActions,
    )

    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  private companion object {
    private const val CRN = "X371199"
    private val notStarted = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplicationId = null,
      action = RuleAction(START_APPROVED_PREMISE_REFERRAL)
    )
    private val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplicationId = null,
    )
    private val upcoming = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      suitableApplicationId = null,
      action = RuleAction("$START_APPROVED_PREMISE_REFERRAL in 2 days", true)
    )
    private val confirmed = ServiceResult(
      serviceStatus = ServiceStatus.CONFIRMED,
      suitableApplicationId = null,
    )
    private val assessing = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = RuleAction(AWAIT_ASSESSMENT, true)
    )
    private val submitted = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = null,
      action = RuleAction(CREATE_PLACEMENT)
    )
    private val withdrawn = ServiceResult(
      serviceStatus = ServiceStatus.WITHDRAWN,
      suitableApplicationId = null,
      action = RuleAction(START_APPROVED_PREMISE_REFERRAL)
    )
    private val rejected = ServiceResult(
      serviceStatus = ServiceStatus.REJECTED,
      suitableApplicationId = null,
      action = RuleAction(START_APPROVED_PREMISE_REFERRAL)
    )

    @JvmStatic
    fun provideEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        CRN,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.NO_ACTION_REQUIRED,
        listOf<String>(),
      ),
      Arguments.of(
        CRN,
        notStarted,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.ACTION_NEEDED,
        listOf(START_APPROVED_PREMISE_REFERRAL),
      ),
      Arguments.of(
        CRN,
        upcoming,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.ACTION_UPCOMING,
        listOf("$START_APPROVED_PREMISE_REFERRAL in 2 days"),
      ),
      Arguments.of(
        CRN,
        confirmed,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.NO_ACTION_REQUIRED,
        listOf<String>(),
      ),
      Arguments.of(
        CRN,
        assessing,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.ACTION_UPCOMING,
        listOf(AWAIT_ASSESSMENT),
      ),
      Arguments.of(
        CRN,
        submitted,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.ACTION_NEEDED,
        listOf(CREATE_PLACEMENT),
      ),
      Arguments.of(
        CRN,
        withdrawn,
        notEligible,
        notEligible,
        notEligible,
        notEligible,
        CaseStatus.ACTION_NEEDED,
        listOf(START_APPROVED_PREMISE_REFERRAL),
      ),
      Arguments.of(
        CRN,
        rejected,
        notEligible,
        notEligible,
        assessing,
        upcoming,
        CaseStatus.ACTION_NEEDED,
        listOf(
          START_APPROVED_PREMISE_REFERRAL,
          AWAIT_ASSESSMENT,
          "$START_APPROVED_PREMISE_REFERRAL in 2 days"
        ),
      ),
    )
  }
}
