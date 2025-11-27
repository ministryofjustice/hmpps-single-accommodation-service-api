package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex

data class DomainData(
  val tier: String,
  val sex: Sex,
)
