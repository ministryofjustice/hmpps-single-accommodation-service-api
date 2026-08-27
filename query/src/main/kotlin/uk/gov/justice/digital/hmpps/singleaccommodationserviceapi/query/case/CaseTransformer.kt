package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    person: PersonDto?,
    cpr: CorePersonRecord?,
    tier: Tier?,
  ) = when (person) {
    is LimitedPersonDto -> person.toLimitedCaseDto()
    is FullPersonDto -> person.toOrchestratedCaseDto(person, cpr, tier)
    null -> CaseDto(crn = crn, userAccess = UserAccess.UNKNOWN, limitedAccess = null)
  }

  private fun FullPersonDto.toOrchestratedCaseDto(
    person: FullPersonDto,
    cpr: CorePersonRecord?,
    tier: Tier?,
  ) = CaseDto(
    forename = cpr?.firstName,
    middleNames = cpr?.middleNames,
    surname = cpr?.lastName,
    dateOfBirth = cpr?.dateOfBirth,
    crn = this.crn,
    prisonNumber = this.nomsNumber,
    tierScore = tier?.tierScore,
    riskLevel = person.riskLevel,
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = person.assignedTo,
    photoUrl = null,
    userAccess = UserAccess.FULL,
    limitedAccess = this.limitedAccess,
  )

  fun PersonDto.toCaseDto(
    caseEntity: CaseEntity?,
  ): CaseDto = when (this) {
    is FullPersonDto -> {
      CaseDto(
        forename = forename,
        middleNames = middleNames,
        surname = surname,
        dateOfBirth = dateOfBirth,
        crn = crn,
        prisonNumber = nomsNumber,
        riskLevel = riskLevel,
        pncReference = pncNumber,
        assignedTo = assignedTo,
        photoUrl = null,
        tierScore = caseEntity?.tierScore,
        userAccess = UserAccess.FULL,
        limitedAccess = this.limitedAccess,
      )
    }

    is LimitedPersonDto -> toLimitedCaseDto()
  }

  fun PersonDto.toCaseDtoV2(
    caseEntity: CaseEntity?,
  ): CaseDto = when (this) {
    is FullPersonDto -> {
      CaseDto(
        forename = caseEntity?.firstName,
        middleNames = null,
        surname = caseEntity?.lastName,
        dateOfBirth = caseEntity?.dateOfBirth,
        crn = crn,
        prisonNumber = nomsNumber,
        riskLevel = riskLevel,
        pncReference = pncNumber,
        assignedTo = assignedTo,
        photoUrl = null,
        tierScore = caseEntity?.tierScore,
        userAccess = UserAccess.FULL,
        limitedAccess = this.limitedAccess,
      )
    }

    is LimitedPersonDto -> toLimitedCaseDto()
  }

  fun PersonDto.toLimitedCaseDto() = CaseDto(
    crn = crn,
    userAccess = UserAccess.LIMITED,
    limitedAccess = true,
  )
}
