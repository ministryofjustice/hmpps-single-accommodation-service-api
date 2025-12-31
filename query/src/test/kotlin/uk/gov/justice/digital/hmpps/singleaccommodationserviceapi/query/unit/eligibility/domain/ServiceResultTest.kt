package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.stream.Stream

class ServiceResultTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.ServiceResultTest#provideCaseStatus",
  )
  fun `should get potential case status based on the service result`(
    serviceResult: ServiceResult,
    caseStatus: CaseStatus,
  ) {
    assertThat(serviceResult.getCaseStatus()).isEqualTo(caseStatus)
  }

  private companion object {
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
    fun provideCaseStatus(): Stream<Arguments> = Stream.of(
      Arguments.of(notEligible, CaseStatus.NO_ACTION_NEEDED),
      Arguments.of(awaitingPlacement, CaseStatus.ACTION_NEEDED),
      Arguments.of(unallocatedAssessment, CaseStatus.ACTION_NEEDED),
      Arguments.of(pendingPlacementRequest, CaseStatus.ACTION_NEEDED),
      Arguments.of(notArrived, CaseStatus.ACTION_NEEDED),
      Arguments.of(departed, CaseStatus.ACTION_NEEDED),
      Arguments.of(cancelled, CaseStatus.ACTION_NEEDED),
      Arguments.of(awaitingAssessment, CaseStatus.ACTION_NEEDED),
      Arguments.of(assessmentInProgress, CaseStatus.ACTION_NEEDED),
      Arguments.of(requestForFurtherInformation, CaseStatus.ACTION_NEEDED),
      Arguments.of(notStarted, CaseStatus.ACTION_NEEDED),
      Arguments.of(upcomingPlacement, CaseStatus.NO_ACTION_NEEDED),
      Arguments.of(upcoming, CaseStatus.ACTION_UPCOMING),
      Arguments.of(arrived, CaseStatus.NO_ACTION_NEEDED),
    )
  }
}
