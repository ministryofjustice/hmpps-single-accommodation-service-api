package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import java.time.LocalDate

data class CaseDto(
  val name: String,
  val dateOfBirth: LocalDate?,
  val crn: String?,
  val prisonNumber: String?,
  val photoUrl: String?,
  val tier: TierScore?,
  val riskLevel: RiskLevel?,
  val pncReference: String?,
  val assignedTo: AssignedToDto?,
  val currentAccommodation: AccommodationDetailDto,
  val nextAccommodation: AccommodationDetailDto,
) {
  constructor(
    crn: String,
    cpr: CorePersonRecord,
    roshDetails: RoshDetails,
    tier: Tier,
    caseSummaries: List<CaseSummary>,
    accommodationResponse: AccommodationResponse,
    photoUrl: String,
  ) : this(
    name = cpr.fullName,
    dateOfBirth = cpr.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers?.firstOrNull(),
    photoUrl = photoUrl,
    tier = tier.tierScore,
    riskLevel = roshDetails.rosh.determineOverallRiskLevel(),
    pncReference = cpr.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto(1L, name = it)
    },
    currentAccommodation = accommodationResponse.current,
    nextAccommodation = accommodationResponse.next,
  )
}

data class AssignedToDto(val id: Long, val name: String)
