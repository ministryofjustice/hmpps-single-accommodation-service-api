package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASES_BY_TEAM
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamCaseList

@Service
class TeamCaseOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val sasAndDeliusCachingService: SasAndDeliusCachingService,
  @param:Value($$"${case-list.page-size:100}") private val pageSize: Long,
) {

  private val log = LoggerFactory.getLogger(javaClass)
  private val initialPage = 0L

  fun getCasesByTeamCode(teamCode: String): OrchestrationResultDto<List<CaseIdentifiers>> {
    log.debug("Retrieving cases from PI for team {}", teamCode)

    val initialCall = mapOf(
      getCallKey(teamCode, initialPage) to {
        sasAndDeliusCachingService.getCasesByTeamCode(teamCode = teamCode, page = initialPage, size = pageSize)
      },
    )

    val initialResultSet = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = initialCall,
    ).standardCallsNoIterationResults!!

    val teamCaseList = initialResultSet.getResult<TeamCaseList>(getCallKey(teamCode, initialPage))

    val (additionalCases, additionalFailures) = teamCaseList?.let { list ->
      log.debug(
        "Received {} cases for team {}, page {} of {}",
        list.content.size,
        teamCode,
        list.page.number + 1,
        list.page.totalPages,
      )

      getRemainingCases(list.page, teamCode)
    } ?: (emptyList<CaseIdentifiers>() to emptyList())

    return OrchestrationResultDto(
      data = teamCaseList?.content?.plus(additionalCases) ?: emptyList(),
      upstreamFailures = initialResultSet.getFailures() + additionalFailures,
    )
  }

  private fun getCallKey(teamCode: String, page: Long) = GET_CASES_BY_TEAM + teamCode + page

  private fun getRemainingCases(page: PageMetadata, teamCode: String) = if (page.number + 1 < page.totalPages) {
    val remainingPages = 1 until page.totalPages
    val resultSet = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = remainingPages.associate { nextPage ->
        getCallKey(teamCode, nextPage) to {
          sasAndDeliusCachingService.getCasesByTeamCode(teamCode = teamCode, page = nextPage, size = pageSize)
        }
      },
    ).standardCallsNoIterationResults!!

    val cases = remainingPages.mapNotNull { nextPage ->
      resultSet.getResult<TeamCaseList>(getCallKey(teamCode, nextPage))?.content
    }.flatten()

    cases to resultSet.getFailures()
  } else {
    null
  }
}
