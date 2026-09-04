package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.util.UUID

data class Cas2Application(
  val uiUrl: String,
  val application: Cas2ApplicationSummary,
)

data class Cas2ApplicationSummary(
  val id: UUID,
  val status: String,
)
