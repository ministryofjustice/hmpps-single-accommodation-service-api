package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata

data class TeamCaseList(
  val content: List<CaseIdentifiers>,
  val page: PageMetadata,
)

data class CaseIdentifiers(
  val crn: String,
  val prisonerNumber: String?,
)
