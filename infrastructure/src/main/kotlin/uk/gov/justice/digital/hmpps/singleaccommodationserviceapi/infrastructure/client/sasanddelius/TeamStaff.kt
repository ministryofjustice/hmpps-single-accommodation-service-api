package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata

data class TeamStaffList(
  val content: List<TeamStaffMember>,
  val page: PageMetadata,
)

data class TeamStaffMember(
  val username: String?,
  val code: String,
  val name: Name,
)
