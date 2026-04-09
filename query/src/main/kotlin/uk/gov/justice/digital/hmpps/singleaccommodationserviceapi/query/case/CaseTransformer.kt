package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {
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
      AssignedToDto(1L, name = it, username = null, staffCode = null)
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    status = null,
    actions = emptyList(),
  )

  fun toCaseDto(
    person: PersonDto,
    caseEntity: CaseEntity?,
    eligibility: EligibilityDto,
  ) = CaseDto(
    name = toFullName(person.name),
    dateOfBirth = person.dateOfBirth,
    crn = person.crn,
    prisonNumber = person.nomsNumber,
    tier = caseEntity?.tierScore?.let { toTierScore(it) },
    // TODO check that we are transforming the risk level correctly
    riskLevel = person.roshLevel?.let { RiskLevel.findByCode(it.code) },
    pncReference = person.pncNumber,
    assignedTo = AssignedToDto(id = 1L, name = toFullName(person.staff.name), username = person.staff.username, staffCode = person.staff.code),
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    tierScore = caseEntity?.tierScore?.let { toTierScore(it) },
    status = null,
    actions = eligibility.caseActions,
  )

  fun toFullName(name: Name) = listOfNotNull(
    name.forename,
    name.middleName?.takeIf { it.isNotBlank() },
    name.surname,
  ).joinToString(" ")

  fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
