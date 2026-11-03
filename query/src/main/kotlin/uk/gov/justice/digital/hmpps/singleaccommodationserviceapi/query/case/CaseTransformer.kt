package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.SexualOffenceRegistrationCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.time.LocalDate

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    case: Case?,
    cpr: CorePersonRecord?,
    tier: Tier?,
    sexualOffenceRegistrations: SexualOffenceRegistrations?,
    userAccess: UserAccess?,
  ) = when {
    case == null -> CaseDto(crn = crn, userAccess = UserAccess.UNKNOWN, limitedAccess = null)

    userAccess == UserAccess.FULL -> case.toOrchestratedCaseDto(
      cpr = cpr,
      tier = tier,
      sexualOffenceRegistrations = sexualOffenceRegistrations,
    )

    else -> toLimitedCaseDto(
      crn = case.crn,
      teamCode = case.team.code,
      assignedTo = case.toAssignedToDto(),
    )
  }

  fun Case.toAssignedToDto() = AssignedToDto(
    forename = this.staff.name.forename,
    surname = this.staff.name.surname,
    username = this.staff.username,
  )

  private fun Case.toOrchestratedCaseDto(
    cpr: CorePersonRecord?,
    tier: Tier?,
    sexualOffenceRegistrations: SexualOffenceRegistrations?,
  ) = CaseDto(
    forename = cpr?.firstName,
    middleNames = cpr?.middleNames,
    surname = cpr?.lastName,
    dateOfBirth = cpr?.dateOfBirth,
    crn = this.crn,
    prisonNumber = this.nomsNumber,
    tierScore = tier?.tierScore,
    riskLevel = this.roshLevel?.let { RiskLevel.findByCode(it.code) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = this.toAssignedToDto(),
    userAccess = UserAccess.FULL,
    limitedAccess = this.limitedAccess ?: false,
    sexualOffenceRegistrations = sexualOffenceRegistrations
      ?.sexualOffenceRegistrations
      ?.map { SexualOffenceRegistrationCode.valueOf(it.type.code.name) }
      .orEmpty(),
  )

  fun toCaseDto(
    caseEntity: CaseEntity?,
    crn: String,
    prisonNumber: String?,
    riskLevel: RiskLevel?,
    assignedTo: AssignedToDto?,
    limitedAccess: Boolean?,
    currentAccommodation: AccommodationSummaryDto? = caseEntity?.currentAccommodation,
    nextAccommodation: AccommodationSummaryDto? = caseEntity?.nextAccommodation,
    teamCode: String?,
  ) = CaseDto(
    forename = caseEntity?.firstName,
    surname = caseEntity?.lastName,
    dateOfBirth = caseEntity?.dateOfBirth,
    crn = crn,
    prisonNumber = prisonNumber,
    riskLevel = riskLevel,
    assignedTo = assignedTo,
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
    teamCode = teamCode,
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

  fun toLimitedCaseDto(crn: String, teamCode: String?, assignedTo: AssignedToDto?) = CaseDto(
    crn = crn,
    userAccess = UserAccess.LIMITED,
    limitedAccess = true,
    teamCode = teamCode,
    assignedTo = assignedTo,
  )
}
