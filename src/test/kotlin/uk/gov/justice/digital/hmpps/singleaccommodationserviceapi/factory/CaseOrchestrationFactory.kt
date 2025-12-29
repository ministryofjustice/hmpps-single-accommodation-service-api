package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun buildCaseOrchestrationDto(
  crn: String,
  cpr: CorePersonRecord = buildCorePersonRecord(),
  roshDetails: RoshDetails = buildRoshDetails(),
  tier: Tier = buildTier(),
  cases: List<CaseSummary> = listOf(buildCaseSummary()),
  accommodationDto: AccommodationDto = buildAccommodationResponse(),
  photoUrl: String = "!!https://www.replace-this-with-a-real-url.com",
) = CaseOrchestrationDto(
  crn,
  cpr,
  roshDetails,
  tier,
  cases,
  accommodationDto = accommodationDto,
  photoUrl = photoUrl,
)
