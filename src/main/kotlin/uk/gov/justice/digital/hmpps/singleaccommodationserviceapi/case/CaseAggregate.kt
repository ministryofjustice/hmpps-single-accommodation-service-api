package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import java.time.LocalDate

class CaseAggregate private constructor(
  private val crn: String,
  private val person: Person,
  private val tier: Tier,
  private val rosh: Rosh,
  private val cases: List<CaseDetail>,
) {

  companion object {
    fun hydrate(dto: CaseOrchestrationDto) = CaseAggregate(
      crn = dto.crn,
      person = Person(
        firstName = dto.cpr.firstName,
        middleNames = dto.cpr.middleNames,
        lastName = dto.cpr.lastName,
        dateOfBirth = dto.cpr.dateOfBirth,
        prisonNumbers = dto.cpr.identifiers?.prisonNumbers ?: emptyList(),
        pncReferences = dto.cpr.identifiers?.pncs ?: emptyList(),
      ),
      tier = Tier(dto.tier.tierScore),
      cases = dto.cases.map {
        CaseDetail(
          team = Team(name = it.manager.team.name),
        )
      },
      rosh = Rosh(
        dto.roshDetails.rosh.riskChildrenCommunity,
        dto.roshDetails.rosh.riskPrisonersCustody,
        dto.roshDetails.rosh.riskStaffCustody,
        dto.roshDetails.rosh.riskStaffCommunity,
        dto.roshDetails.rosh.riskKnownAdultCustody,
        dto.roshDetails.rosh.riskKnownAdultCommunity,
        dto.roshDetails.rosh.riskPublicCustody,
        dto.roshDetails.rosh.riskPublicCommunity,
        dto.roshDetails.rosh.riskChildrenCustody,
      ),
    )
  }

  fun getCaseDto() = CaseDto(
    name = person.fullName,
    dateOfBirth = person.dateOfBirth,
    crn = crn,
    prisonNumber = person.prisonNumbers.firstOrNull(),
    tier = tier.tierScore,
    riskLevel = rosh.determineOverallRiskLevel(),
    pncReference = person.pncReferences.firstOrNull(),
    assignedTo = cases.firstOrNull()?.team?.name?.let {
      AssignedToDto(1L, name = it)
    },
    currentAccommodation = CurrentAccommodationDto("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodationDto("PRISON", LocalDate.now().plusDays(100)),
  )
}

data class Person(
  val firstName: String?,
  val middleNames: String?,
  val lastName: String?,
  val dateOfBirth: LocalDate?,
  val prisonNumbers: List<String>,
  val pncReferences: List<String>,
) {
  val fullName: String
    get() = listOfNotNull(
      firstName,
      middleNames?.takeIf { it.isNotBlank() },
      lastName,
    ).joinToString(" ")
}

data class Tier(val tierScore: String)

data class CaseDetail(
  val team: Team,
)

data class Team(
  val name: String,
)

data class Rosh(
  val riskChildrenCommunity: RiskLevel? = null,
  val riskPrisonersCustody: RiskLevel? = null,
  val riskStaffCustody: RiskLevel? = null,
  val riskStaffCommunity: RiskLevel? = null,
  val riskKnownAdultCustody: RiskLevel? = null,
  val riskKnownAdultCommunity: RiskLevel? = null,
  val riskPublicCustody: RiskLevel? = null,
  val riskPublicCommunity: RiskLevel? = null,
  val riskChildrenCustody: RiskLevel? = null,
) {
  fun determineOverallRiskLevel() = listOfNotNull(
    riskChildrenCommunity,
    riskPrisonersCustody,
    riskStaffCommunity,
    riskStaffCustody,
    riskKnownAdultCommunity,
    riskKnownAdultCustody,
    riskPublicCommunity,
    riskPublicCustody,
  ).maxByOrNull { it.priority }
}
