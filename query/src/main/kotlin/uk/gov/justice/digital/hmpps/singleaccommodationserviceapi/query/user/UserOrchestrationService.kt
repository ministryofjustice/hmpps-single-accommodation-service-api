package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.fetchAllPages
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

  fun getStaffForTeams(teamCodes: List<String>): OrchestrationResultDto<List<TeamStaffMember>> {
    log.debug("Retrieving staff from Delius for teams {}", teamCodes)

    val staff = aggregatorService.fetchAllPages(
      callKey = { page -> GET_STAFF_FOR_TEAMS + page },
      fetchPage = { page ->
        sasAndDeliusCachingService.getStaffForTeams(teamCodes = teamCodes, page = page, size = pageSize)
      },
      pageMetadata = TeamStaffList::page,
      content = TeamStaffList::content,
    )
    log.debug("Received {} staff", staff.data.size)

    return staff
  }
}
