package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata

data class TeamCaseList(
  val cases: List<TeamCaseIdentifiers>,
  val page: PageMetadata,
)

data class TeamCaseIdentifiers(
  val crn: String,
  val prisonNumber: String?,
)
