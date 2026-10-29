package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.POST_CASE_SUMMARIES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService

@Service
class CustomCaseListOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val limitedAccessService: LimitedAccessService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
) {
  fun getCaseAccessAndSummaries(username: String, crns: List<String>): OrchestrationResultDto<CustomCaseListOrchestrationDto> {
    val calls = limitedAccessService.userAccessCalls(username, crns) + mapOf(
      POST_CASE_SUMMARIES to { approvedPremisesAndDeliusCachingService.postCaseSummaries(crns) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    return OrchestrationResultDto(
      data = CustomCaseListOrchestrationDto(
        caseAccess = limitedAccessService.getCaseAccess(results.standardCallsNoIterationResults!!),
        caseSummaries = results.standardCallsNoIterationResults!!.getResult<CaseSummaries>(POST_CASE_SUMMARIES)?.cases
          ?: emptyList(),
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
