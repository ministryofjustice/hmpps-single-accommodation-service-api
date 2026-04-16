package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordAddresses

data class AccommodationOrchestrationDto(
  val cpr: CorePersonRecord?,
  val cprAddresses: CorePersonRecordAddresses?,
)
