package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.POST_CASE_SUMMARIES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.ProbationAccessControlService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess

@Service
class CustomCaseListOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val probationAccessControlService: ProbationAccessControlService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
) {
  fun getCaseAccessAndSummaries(username: String, crns: List<String>): OrchestrationResultDto<CustomCaseListOrchestrationDto> {
    val calls = mapOf(
      GET_USER_ACCESS to { probationAccessControlService.getUserAccess(username, crns) },
      POST_CASE_SUMMARIES to { approvedPremisesAndDeliusCachingService.postCaseSummaries(crns) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    return OrchestrationResultDto(
      data = CustomCaseListOrchestrationDto(
        userCaseAccess = results.standardCallsNoIterationResults!!.getResult<UserCaseAccess>(GET_USER_ACCESS),
        caseSummaries = results.standardCallsNoIterationResults!!.getResult<CaseSummaries>(POST_CASE_SUMMARIES)?.cases
          ?: emptyList(),
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
