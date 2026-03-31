package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {

  // TODO: remove once we are getting data from sas_case
  fun toCaseDto(
    person: PersonDto,
  ) = CaseDto(
    name = person.name,
    dateOfBirth = person.dateOfBirth,
    crn = person.crn,
    prisonNumber = person.nomsNumber,
    riskLevel = person.roshLevelCode?.let { RiskLevel.valueOf(it) },
    pncReference = person.pncNumber,
    assignedTo = person.staff.let {
      AssignedToDto(
        id = null,
        name = toFullName(it.name),
        username = it.username,
      )
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    tierScore = TierScore.A1,
    tier = TierScore.A1,
    status = Status.RISK_OF_NO_FIXED_ABODE,
    actions = emptyList(),
  )

  fun toCaseDto(
    crn: String,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    caseSummaries: List<CaseSummary>?,
  ) = CaseDto(
    name = cpr?.let { toFullName(it) },
    dateOfBirth = cpr?.dateOfBirth,
    crn = crn,
    prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
    tierScore = tier?.let { toTierScore(tier.tierScore) },
    tier = tier?.let { toTierScore(tier.tierScore) },
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries?.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto(1L, name = it, username = it)
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    status = Status.RISK_OF_NO_FIXED_ABODE,
    actions = emptyList(),
  )

  fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
