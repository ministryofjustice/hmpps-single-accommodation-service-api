package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.fetchAllPages
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASES_BY_TEAM
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamCaseList

@Service
class TeamCaseOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val sasAndDeliusClient: SasAndDeliusClient,
  @param:Value($$"${bulk-load.page-size:10000}") private val pageSize: Long,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun getCasesByTeamCode(teamCode: String): OrchestrationResultDto<List<CaseIdentifiers>> {
    log.debug("Retrieving cases from PI for team {}", teamCode)

    val cases = aggregatorService.fetchAllPages(
      callKey = { page -> GET_CASES_BY_TEAM + teamCode + page },
      fetchPage = { page ->
        sasAndDeliusClient.getCasesByTeamCode(teamCode = teamCode, page = page, size = pageSize)
      },
      pageMetadata = TeamCaseList::page,
      content = TeamCaseList::content,
    )
    log.debug("Received {} cases for team {}", cases.data.size, teamCode)

    return cases
  }
}
