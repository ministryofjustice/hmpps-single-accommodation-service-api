package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier

fun buildCaseOrchestrationDto(
  crn: String,
  cpr: CorePersonRecord = buildCorePersonRecord(),
  roshDetails: RoshDetails = buildRoshDetails(),
  tier: Tier = buildTier(),
  cases: List<CaseSummary> = listOf(buildCaseSummary()),
  accommodationStatus: AccommodationStatus = buildAccommodationStatus(),
  photoUrl: String = "!!https://www.replace-this-with-a-real-url.com",
) = CaseOrchestrationDto(
  crn,
  cpr,
  roshDetails,
  tier,
  cases,
  accommodationStatus = accommodationStatus,
  photoUrl = photoUrl,
)
