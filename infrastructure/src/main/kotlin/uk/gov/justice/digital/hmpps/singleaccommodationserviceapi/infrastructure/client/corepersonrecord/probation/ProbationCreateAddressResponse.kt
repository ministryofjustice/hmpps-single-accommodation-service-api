package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation

import java.util.UUID

data class ProbationCreateAddressResponse(
  val crn: String,
  val cprAddressId: UUID,
)
