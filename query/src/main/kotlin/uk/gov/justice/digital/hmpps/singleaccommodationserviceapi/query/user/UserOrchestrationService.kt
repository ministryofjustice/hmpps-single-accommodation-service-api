package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_STAFF_FOR_TEAMS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamStaffList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamStaffMember

@Service
class UserOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val sasAndDeliusCachingService: SasAndDeliusCachingService,
  @param:Value($$"${team-members.page-size:200}") private val pageSize: Long,
) {

  private val log = LoggerFactory.getLogger(javaClass)
  private val initialPage = 0L

  fun getStaffForTeams(teamCodes: List<String>): OrchestrationResultDto<List<TeamStaffMember>> {
    log.debug("Retrieving staff from Delius for teams {}", teamCodes)

    val initialCall = mapOf(
      getCallKey(initialPage) to {
        sasAndDeliusCachingService.getStaffForTeams(teamCodes = teamCodes, page = initialPage, size = pageSize)
      },
    )

    val initialResultSet = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = initialCall,
    ).standardCallsNoIterationResults!!

    val teamStaffList = initialResultSet.getResult<TeamStaffList>(getCallKey(initialPage))

    val (additionalStaff, additionalFailures) = teamStaffList?.let { list ->
      log.debug(
        "Received {} staff, page {} of {}",
        list.content.size,
        list.page.number + 1,
        list.page.totalPages,
      )

      getRemainingStaff(list.page, teamCodes)
    } ?: (emptyList<TeamStaffMember>() to emptyList())

    return OrchestrationResultDto(
      data = teamStaffList?.content?.plus(additionalStaff) ?: emptyList(),
      upstreamFailures = initialResultSet.getFailures() + additionalFailures,
    )
  }

  private fun getCallKey(page: Long) = GET_STAFF_FOR_TEAMS + page

  private fun getRemainingStaff(page: PageMetadata, teamCodes: List<String>) = if (page.number + 1 < page.totalPages) {
    val remainingPages = 1 until page.totalPages
    val resultSet = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = remainingPages.associate { nextPage ->
        getCallKey(nextPage) to {
          sasAndDeliusCachingService.getStaffForTeams(teamCodes = teamCodes, page = nextPage, size = pageSize)
        }
      },
    ).standardCallsNoIterationResults!!

    val staff = remainingPages.mapNotNull { nextPage ->
      resultSet.getResult<TeamStaffList>(getCallKey(nextPage))?.content
    }.flatten()

    staff to resultSet.getFailures()
  } else {
    null
  }
}
