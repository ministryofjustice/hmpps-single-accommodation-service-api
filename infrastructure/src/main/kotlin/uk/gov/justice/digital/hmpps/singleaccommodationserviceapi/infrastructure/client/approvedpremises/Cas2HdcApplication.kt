package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.util.UUID

data class Cas2HdcApplication(
  val crn: String,
  val id: UUID,
)
