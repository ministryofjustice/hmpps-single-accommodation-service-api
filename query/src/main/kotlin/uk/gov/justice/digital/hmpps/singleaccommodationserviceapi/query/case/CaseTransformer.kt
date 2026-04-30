package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FullCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    case: Case,
  ) = FullCaseDto(
    name = cpr?.let { toFullName(it) },
    dateOfBirth = cpr?.dateOfBirth,
    crn = crn,
    prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
    tierScore = tier?.let { toTierScore(tier.tierScore) },
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = AssignedToDto(
      name = case.staff.name.fullName,
      username = case.staff.username,
      staffCode = case.staff.code,
    ),
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
  ) = FullCaseDto(
    name = person.name,
    dateOfBirth = person.dateOfBirth,
    crn = person.crn,
    prisonNumber = person.nomsNumber,
    // TODO check that we are transforming the risk level correctly
    riskLevel = person.roshLevel,
    pncReference = person.pncNumber,
    assignedTo = AssignedToDto(
      name = person.staff.name.fullName,
      username = person.staff.username,
      staffCode = person.staff.code,
    ),
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    tierScore = caseEntity?.tierScore?.let { toTierScore(it) },
    status = null,
    actions = eligibility.caseActions,
  )

  fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
