package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier

data class RulesOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord,
  val tier: Tier,
)
