package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    person: PersonDto?,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
  ) = when (person) {
    is ExcludedPersonDto -> person.excluded()
    is RestrictedPersonDto -> toOrchestratedCaseDto(person, cpr, roshDetails, tier, CaseAccess.RESTRICTED)
    is FullPersonDto -> toOrchestratedCaseDto(person, cpr, roshDetails, tier, CaseAccess.FULL)
    null -> CaseDto(crn = crn, caseAccess = CaseAccess.UNKNOWN)
  }

  private fun toOrchestratedCaseDto(
    person: PersonDto,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    caseAccess: CaseAccess,
  ) = CaseDto(
    name = cpr?.toFullName(),
    dateOfBirth = cpr?.dateOfBirth,
    crn = person.crn,
    prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
    tierScore = tier?.let { toTierScore(tier.tierScore) },
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = person.assignedTo,
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    status = null,
    actions = emptyList(),
    caseAccess = caseAccess,
  )

  fun PersonDto.toCaseDto(
    caseEntity: CaseEntity?,
    eligibility: EligibilityDto?,
  ): CaseDto = when (this) {
    is FullPersonDto -> {
      CaseDto(
        name = name,
        dateOfBirth = dateOfBirth,
        crn = crn,
        prisonNumber = nomsNumber,
        riskLevel = roshLevel,
        pncReference = pncNumber,
        assignedTo = assignedTo,
        photoUrl = null,
        currentAccommodation = null,
        nextAccommodation = null,
        tierScore = caseEntity?.tierScore?.let { toTierScore(it) },
        status = null,
        actions = eligibility?.caseActions.orEmpty(),
        caseAccess = CaseAccess.FULL,
      )
    }

    is RestrictedPersonDto -> {
      CaseDto(
        name = name,
        dateOfBirth = dateOfBirth,
        crn = crn,
        prisonNumber = nomsNumber,
        riskLevel = roshLevel,
        pncReference = pncNumber,
        assignedTo = assignedTo,
        photoUrl = null,
        currentAccommodation = null,
        nextAccommodation = null,
        tierScore = caseEntity?.tierScore?.let { toTierScore(it) },
        status = null,
        actions = eligibility?.caseActions.orEmpty(),
        caseAccess = CaseAccess.RESTRICTED,
      )
    }

    is ExcludedPersonDto -> excluded()
  }

  fun PersonDto.excluded() = CaseDto(
    crn = crn,
    prisonNumber = nomsNumber,
    assignedTo = assignedTo,
    caseAccess = CaseAccess.EXCLUDED,
  )

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
