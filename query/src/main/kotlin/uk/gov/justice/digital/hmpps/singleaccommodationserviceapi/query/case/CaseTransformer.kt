package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.IndividualName
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
    tier = tier?.let { toTierScore(tier.tierScore) },
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries?.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto("1L", name = it)
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    eligibilityDto = null,
  )

  fun toCaseDto(
    caseListItem: Case,
    caseEntity: CaseEntity?,
    eligibilityDto: EligibilityDto?,
  ) = CaseDto(
    name = toFullName(caseListItem.name),
    dateOfBirth = caseListItem.dateOfBirth,
    crn = caseListItem.crn,
    prisonNumber = caseListItem.nomsNumber,
    tier = caseEntity?.tier?.let { toTierScore(it) },
    // TODO check that we are transforming the risk level correctly
    riskLevel = caseListItem.roshLevel?.let { RiskLevel.valueOf(it.code) },
    pncReference = caseListItem.pncNumber,
    assignedTo = AssignedToDto(caseListItem.staff.code, name = toFullName(caseListItem.staff.name)),
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    eligibilityDto = eligibilityDto,
  )

  fun toFullName(name: IndividualName) = listOfNotNull(
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
