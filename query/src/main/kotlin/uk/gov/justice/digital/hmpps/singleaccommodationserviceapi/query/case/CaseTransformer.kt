package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    person: PersonDto?,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
  ) = when (person) {
    is LimitedPersonDto -> person.limited()
    is FullPersonDto -> toOrchestratedCaseDto(person, cpr, roshDetails, tier, UserAccess.FULL, person.limitedAccess)
    null -> CaseDto(crn = crn, userAccess = UserAccess.UNKNOWN, limitedAccess = null)
  }

  private fun toOrchestratedCaseDto(
    person: PersonDto,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    userAccess: UserAccess,
    limitedAccess: Boolean,
  ) = CaseDto(
    name = cpr?.toFullName(),
    dateOfBirth = cpr?.dateOfBirth,
    crn = person.crn,
    prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
    tierScore = tier?.tierScore,
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = person.assignedTo,
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    status = null,
    actions = emptyList(),
    userAccess = userAccess,
    limitedAccess = limitedAccess,
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
        tierScore = caseEntity?.tierScore,
        status = null,
        actions = eligibility?.caseActions.orEmpty(),
        userAccess = UserAccess.FULL,
        limitedAccess = this.limitedAccess,
      )
    }

    is LimitedPersonDto -> limited()
  }

  fun PersonDto.limited() = CaseDto(
    crn = crn,
    userAccess = UserAccess.LIMITED,
    limitedAccess = true,
  )
}
