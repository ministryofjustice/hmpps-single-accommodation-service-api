package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LAOStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {
  fun toCaseDto(
    person: PersonDto,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
  ) = when (person) {
    is ExcludedPersonDto -> person.excluded()
    is RestrictedPersonDto -> toOrchestratedCaseDto(person, cpr, roshDetails, tier, LAOStatus.RESTRICTED)
    is FullPersonDto -> toOrchestratedCaseDto(person, cpr, roshDetails, tier, LAOStatus.NONE)
  }

  private fun toOrchestratedCaseDto(
    person: PersonDto,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    laoStatus: LAOStatus,
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
    laoStatus = laoStatus,
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
        laoStatus = LAOStatus.NONE,
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
        laoStatus = LAOStatus.RESTRICTED,
      )
    }

    is ExcludedPersonDto -> excluded()
  }

  fun PersonDto.excluded() = CaseDto(
    crn = crn,
    prisonNumber = nomsNumber,
    assignedTo = assignedTo,
    laoStatus = LAOStatus.EXCLUDED,
  )

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
