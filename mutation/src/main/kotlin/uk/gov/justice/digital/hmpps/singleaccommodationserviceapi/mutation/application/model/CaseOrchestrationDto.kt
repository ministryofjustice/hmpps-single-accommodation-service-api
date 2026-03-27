package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.model

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier

data class CaseOrchestrationDto(
  val crn: String,
  val tier: Tier,
  val cas1Application: Cas1Application?,
)
