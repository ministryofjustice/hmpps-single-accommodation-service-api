package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.time.LocalDate

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
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
  ): CaseDto = when (this) {
    is FullPersonDto -> toCaseDto(
      caseEntity = caseEntity,
      crn = crn,
      prisonNumber = nomsNumber,
      riskLevel = riskLevel,
      pncReference = pncNumber,
      assignedTo = assignedTo,
      limitedAccess = limitedAccess,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
    )

    is LimitedPersonDto -> toLimitedCaseDto()
  }

  fun toCaseDto(
    caseEntity: CaseEntity?,
    crn: String,
    prisonNumber: String?,
    riskLevel: RiskLevel?,
    pncReference: String?,
    assignedTo: AssignedToDto?,
    limitedAccess: Boolean?,
    currentAccommodation: AccommodationSummaryDto? = caseEntity?.currentAccommodation,
    nextAccommodation: AccommodationSummaryDto? = caseEntity?.nextAccommodation,
  ) = CaseDto(
    forename = caseEntity?.firstName,
    middleNames = null,
    surname = caseEntity?.lastName,
    dateOfBirth = caseEntity?.dateOfBirth,
    crn = crn,
    prisonNumber = prisonNumber,
    riskLevel = riskLevel,
    pncReference = pncReference,
    assignedTo = assignedTo,
    photoUrl = null,
    tierScore = caseEntity?.tierScore,
    userAccess = UserAccess.FULL,
    limitedAccess = limitedAccess,
    accommodationSummaries = caseEntity?.let {
      toAccommodationSummariesDto(
        accommodationStatus = it.accommodationStatus,
        accommodationStatusDate = it.accommodationStatusDate,
        currentAccommodation = currentAccommodation,
        nextAccommodation = nextAccommodation,
      )
    },
  )

  fun toAccommodationSummariesDto(
    accommodationStatus: CaseAccommodationStatus?,
    accommodationStatusDate: LocalDate?,
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
  ) = AccommodationSummariesDto(
    caseAccommodationStatus = accommodationStatus,
    caseAccommodationStatusDate = accommodationStatusDate,
    currentAccommodation = currentAccommodation,
    nextAccommodation = nextAccommodation,
  )

  fun PersonDto.toLimitedCaseDto() = toLimitedCaseDto(crn)

  fun toLimitedCaseDto(crn: String) = CaseDto(
    crn = crn,
    userAccess = UserAccess.LIMITED,
    limitedAccess = true,
  )
}
