package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import java.time.LocalDate

data class CaseDto(
  val name: String,
  val dateOfBirth: LocalDate?,
  val crn: String?,
  val prisonNumber: String?,
  val tier: String?,
  val riskLevel: RiskLevel?,
  val pncReference: String?,
  val assignedTo: AssignedToDto?,
  val currentAccommodation: CurrentAccommodationDto?,
  val nextAccommodation: NextAccommodationDto?,
) {
  constructor(crn: String, cpr: CorePersonRecord, roshDetails: RoshDetails, tier: Tier, caseSummaries: List<CaseSummary>) : this(
    name = cpr.fullName,
    dateOfBirth = cpr.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers?.firstOrNull(),
    tier = tier.tierScore,
    riskLevel = roshDetails.rosh.determineOverallRiskLevel(),
    pncReference = cpr.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto(1L, name = it)
    },
    currentAccommodation = CurrentAccommodationDto("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodationDto("PRISON", LocalDate.now().plusDays(100)),
  )
}

data class AssignedToDto(val id: Long, val name: String)
data class CurrentAccommodationDto(val type: String, val endDate: LocalDate)
data class NextAccommodationDto(val type: String, val startDate: LocalDate)
