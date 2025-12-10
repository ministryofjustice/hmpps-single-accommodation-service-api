package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier

data class EligibilityOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord,
  val tier: Tier,
  val cas1Application: Cas1Application?,
)
