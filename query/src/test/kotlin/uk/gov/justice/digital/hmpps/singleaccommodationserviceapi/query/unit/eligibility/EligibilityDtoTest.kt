package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.stream.Stream

class EligibilityDtoTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityDtoTest#provideEligibility",
  )
  fun `should transform to eligibility`(
    actualEligibility: EligibilityDto,
    expectedEligibility: EligibilityDto,
  ) {
    assertThat(actualEligibility).isEqualTo(expectedEligibility)
  }

  private companion object {
    private const val CRN = "X371199"
    private val arrived = ServiceResult(
      serviceStatus = ServiceStatus.ARRIVED,
      suitableApplication = null,
      actions = listOf("ARRIVED action 1", "ARRIVED action 2"),
    )
    private val upcoming = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      suitableApplication = null,
      actions = listOf("UPCOMING action 1", "UPCOMING action 2"),
    )
    private val upcomingPlacement = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING_PLACEMENT,
      suitableApplication = null,
      actions = listOf("UPCOMING_PLACEMENT action 1", "UPCOMING_PLACEMENT action 2"),
    )
    private val notStarted = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplication = null,
      actions = listOf("NOT_STARTED action 1", "NOT_STARTED action 2"),
    )
    private val requestForFurtherInformation = ServiceResult(
      serviceStatus = ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION,
      suitableApplication = null,
      actions = listOf("REQUEST_FOR_FURTHER_INFORMATION action 1", "REQUEST_FOR_FURTHER_INFORMATION action 2"),
    )
    private val assessmentInProgress = ServiceResult(
      serviceStatus = ServiceStatus.ASSESSMENT_IN_PROGRESS,
      suitableApplication = null,
      actions = listOf("ASSESSMENT_IN_PROGRESS action 1", "ASSESSMENT_IN_PROGRESS action 2"),
    )
    private val awaitingAssessment = ServiceResult(
      serviceStatus = ServiceStatus.AWAITING_ASSESSMENT,
      suitableApplication = null,
      actions = listOf("AWAITING_ASSESSMENT action 1", "AWAITING_ASSESSMENT action 2"),
    )
    private val cancelled = ServiceResult(
      serviceStatus = ServiceStatus.CANCELLED,
      suitableApplication = null,
      actions = listOf("CANCELLED action 1", "CANCELLED action 2"),
    )
    private val departed = ServiceResult(
      serviceStatus = ServiceStatus.DEPARTED,
      suitableApplication = null,
      actions = listOf("DEPARTED action 1", "DEPARTED action 2"),
    )
    private val notArrived = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ARRIVED,
      suitableApplication = null,
      actions = listOf("NOT_ARRIVED action 1", "NOT_ARRIVED action 2"),
    )
    private val pendingPlacementRequest = ServiceResult(
      serviceStatus = ServiceStatus.PENDING_PLACEMENT_REQUEST,
      suitableApplication = null,
      actions = listOf("PENDING_PLACEMENT_REQUEST action 1", "PENDING_PLACEMENT_REQUEST action 2"),
    )
    private val unallocatedAssessment = ServiceResult(
      serviceStatus = ServiceStatus.UNALLOCATED_ASSESSMENT,
      suitableApplication = null,
      actions = listOf("UNALLOCATED_ASSESSMENT action 1", "UNALLOCATED_ASSESSMENT action 2"),
    )
    private val awaitingPlacement = ServiceResult(
      serviceStatus = ServiceStatus.AWAITING_PLACEMENT,
      suitableApplication = null,
      actions = listOf("AWAITING_PLACEMENT action 1", "AWAITING_PLACEMENT action 2"),
    )
    private val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplication = null,
      actions = listOf("NOT_ELIGIBLE action 1", "NOT_ELIGIBLE action 2"),
    )

    @JvmStatic
    fun provideEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        EligibilityDto(
          crn = CRN,
          cas1 = arrived,
          cas2Hdc = upcoming,
          cas2PrisonBail = upcomingPlacement,
          cas2CourtBail = notStarted,
          cas3 = requestForFurtherInformation,
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = arrived,
          cas2Hdc = upcoming,
          cas2PrisonBail = upcomingPlacement,
          cas2CourtBail = notStarted,
          cas3 = requestForFurtherInformation,
          caseStatus = CaseStatus.ACTION_NEEDED,
          caseActions = listOf(
            "ARRIVED action 1",
            "ARRIVED action 2",
            "UPCOMING action 1",
            "UPCOMING action 2",
            "NOT_STARTED action 1",
            "NOT_STARTED action 2",
            "UPCOMING_PLACEMENT action 1",
            "UPCOMING_PLACEMENT action 2",
            "REQUEST_FOR_FURTHER_INFORMATION action 1",
            "REQUEST_FOR_FURTHER_INFORMATION action 2",
          ),
        ),
      ),
      Arguments.of(
        EligibilityDto(
          crn = CRN,
          cas1 = assessmentInProgress,
          cas2Hdc = unallocatedAssessment,
          cas2PrisonBail = cancelled,
          cas2CourtBail = departed,
          cas3 = notEligible,
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = assessmentInProgress,
          cas2Hdc = unallocatedAssessment,
          cas2PrisonBail = cancelled,
          cas2CourtBail = departed,
          cas3 = notEligible,
          caseStatus = CaseStatus.ACTION_NEEDED,
          caseActions = listOf(
            "ASSESSMENT_IN_PROGRESS action 1",
            "ASSESSMENT_IN_PROGRESS action 2",
            "UNALLOCATED_ASSESSMENT action 1",
            "UNALLOCATED_ASSESSMENT action 2",
            "DEPARTED action 1",
            "DEPARTED action 2",
            "CANCELLED action 1",
            "CANCELLED action 2",
            "NOT_ELIGIBLE action 1",
            "NOT_ELIGIBLE action 2",
          ),
        ),
      ),
      Arguments.of(
        EligibilityDto(
          crn = CRN,
          cas1 = pendingPlacementRequest,
          cas2Hdc = awaitingAssessment,
          cas2PrisonBail = notArrived,
          cas2CourtBail = null,
          cas3 = awaitingPlacement,
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = pendingPlacementRequest,
          cas2Hdc = awaitingAssessment,
          cas2PrisonBail = notArrived,
          cas2CourtBail = null,
          cas3 = awaitingPlacement,
          caseStatus = CaseStatus.ACTION_NEEDED,
          caseActions = listOf(
            "PENDING_PLACEMENT_REQUEST action 1",
            "PENDING_PLACEMENT_REQUEST action 2",
            "AWAITING_ASSESSMENT action 1",
            "AWAITING_ASSESSMENT action 2",
            "NOT_ARRIVED action 1",
            "NOT_ARRIVED action 2",
            "AWAITING_PLACEMENT action 1",
            "AWAITING_PLACEMENT action 2",
          ),
        ),
      ),
      Arguments.of(
        EligibilityDto(
          crn = CRN,
          cas1 = notEligible,
          cas2Hdc = upcomingPlacement,
          cas2PrisonBail = null,
          cas2CourtBail = arrived,
          cas3 = notEligible,
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = notEligible,
          cas2Hdc = upcomingPlacement,
          cas2PrisonBail = null,
          cas2CourtBail = arrived,
          cas3 = notEligible,
          caseStatus = CaseStatus.NO_ACTION_NEEDED,
          caseActions = listOf(
            "NOT_ELIGIBLE action 1",
            "NOT_ELIGIBLE action 2",
            "UPCOMING_PLACEMENT action 1",
            "UPCOMING_PLACEMENT action 2",
            "ARRIVED action 1",
            "ARRIVED action 2",
            "NOT_ELIGIBLE action 1",
            "NOT_ELIGIBLE action 2",
          ),
        ),
      ),
      Arguments.of(
        EligibilityDto(
          crn = CRN,
          cas1 = upcoming,
          cas2Hdc = upcoming,
          cas2PrisonBail = arrived,
          cas2CourtBail = notEligible,
          cas3 = upcomingPlacement,
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = upcoming,
          cas2Hdc = upcoming,
          cas2PrisonBail = arrived,
          cas2CourtBail = notEligible,
          cas3 = upcomingPlacement,
          caseStatus = CaseStatus.ACTION_UPCOMING,
          caseActions = listOf(
            "UPCOMING action 1",
            "UPCOMING action 2",
            "UPCOMING action 1",
            "UPCOMING action 2",
            "NOT_ELIGIBLE action 1",
            "NOT_ELIGIBLE action 2",
            "ARRIVED action 1",
            "ARRIVED action 2",
            "UPCOMING_PLACEMENT action 1",
            "UPCOMING_PLACEMENT action 2",
          ),
        ),
      ),
    )
  }
}
