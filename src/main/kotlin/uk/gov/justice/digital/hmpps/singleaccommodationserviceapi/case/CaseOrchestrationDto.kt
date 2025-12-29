package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier

data class CaseOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord,
  val roshDetails: RoshDetails,
  val tier: Tier,
  val cases: List<CaseSummary>,
  val accommodationDto: AccommodationDto?,
  val photoUrl: String?,
)
