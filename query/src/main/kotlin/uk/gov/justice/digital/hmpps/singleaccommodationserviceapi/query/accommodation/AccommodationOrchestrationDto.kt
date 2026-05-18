package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord

data class AccommodationOrchestrationDto(
  val cpr: CorePersonRecord?,
)
