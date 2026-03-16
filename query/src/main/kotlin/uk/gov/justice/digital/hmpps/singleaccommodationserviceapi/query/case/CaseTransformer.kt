package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseListItem
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.StaffName
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
    name = cpr?.let { toFullName(it) } ?: error("no name for $crn"),
    dateOfBirth = cpr.dateOfBirth ?: error("no dateOfBirth for $crn"),
    crn = crn,
    prisonNumber = cpr.identifiers!!.prisonNumbers.firstOrNull() ?: error("no prisonNumber for $crn"),
    tier = tier?.let { toTierScore(tier.tierScore) } ?: error("no tier for $crn"),
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) } ?: error("no riskLevel for $crn"),
    pncReference = cpr.identifiers?.pncs?.firstOrNull() ?: error("no pncReference for $crn"),
    assignedTo = caseSummaries?.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto("1L", name = it)
    } ?: error("no assignedTo for $crn"),
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    eligibility = null,
  )

  fun toCaseDtoFromCaseEntity(
    caseEntity: CaseEntity,
    eligibility: EligibilityDto,
    caseListItem: CaseListItem,
  ) = CaseDto(
    name = toFullName(caseListItem.name),
    dateOfBirth = caseListItem.dateOfBirth,
    crn = caseEntity.crn,
    prisonNumber = caseListItem.nomsNumber ?: error("no prisonNumber for ${caseEntity.crn}"),
    tier = caseEntity.tier?.let { toTierScore(it) } ?: error("no tier for ${caseEntity.crn}"),
    // TODO transform properly
    riskLevel = RiskLevel.valueOf(caseListItem.roshLevel!!.code),
    pncReference = caseListItem.pncNumber ?: error("no pncReference for ${caseEntity.crn}"),
    assignedTo = AssignedToDto(caseListItem.staff.code, name = toStaffFullName(caseListItem.staff.name)),
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    eligibility = eligibility,
  )

  fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toStaffFullName(name: StaffName) = listOfNotNull(
    name.forename,
    name.surname,
  ).joinToString(" ")

  fun toFullName(name: Name) = listOfNotNull(
    name.forename,
    name.middleName,
    name.surname,
  ).joinToString(" ")

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
