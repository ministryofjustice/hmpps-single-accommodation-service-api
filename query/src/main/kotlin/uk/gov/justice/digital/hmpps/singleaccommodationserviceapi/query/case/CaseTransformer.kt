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
    inUsersCustomList: Boolean,
  ) = when {
    case == null -> CaseDto(crn = crn, userAccess = UserAccess.UNKNOWN, limitedAccess = null, inUsersCustomList = inUsersCustomList)

    userAccess == UserAccess.FULL -> case.toOrchestratedCaseDto(
      cpr = cpr,
      tier = tier,
      sexualOffenceRegistrations = sexualOffenceRegistrations,
      inUsersCustomList = inUsersCustomList,
    )

    else -> toLimitedCaseDto(
      crn = case.crn,
      teamCode = case.team.code,
      assignedTo = case.toAssignedToDto(),
      inUsersCustomList = inUsersCustomList,
      accommodationStatus = null,
      accommodationStatusDate = null,
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
    inUsersCustomList: Boolean,
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
    inUsersCustomList = inUsersCustomList,
  )

  fun toCaseDto(
    caseEntity: CaseEntity?,
    crn: String,
    assignedTo: AssignedToDto?,
    limitedAccess: Boolean?,
    teamCode: String?,
    inUsersCustomList: Boolean,
  ) = CaseDto(
    forename = caseEntity?.firstName,
    surname = caseEntity?.lastName,
    dateOfBirth = caseEntity?.dateOfBirth,
    crn = crn,
    prisonNumber = caseEntity?.latestPrisonNumber(),
    riskLevel = caseEntity?.roshLevelCode?.let { RiskLevel.findByCode(it) },
    assignedTo = assignedTo,
    tierScore = caseEntity?.tierScore,
    userAccess = UserAccess.FULL,
    limitedAccess = limitedAccess,
    accommodationSummaries = caseEntity?.let {
      toAccommodationSummariesDto(
        accommodationStatus = it.accommodationStatus,
        accommodationStatusDate = it.accommodationStatusDate,
        currentAccommodation = it.currentAccommodation,
        nextAccommodation = it.nextAccommodation,
      )
    },
    teamCode = teamCode,
    inUsersCustomList = inUsersCustomList,
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

  fun toLimitedCaseDto(crn: String, teamCode: String?, assignedTo: AssignedToDto?, inUsersCustomList: Boolean, accommodationStatus: CaseAccommodationStatus?, accommodationStatusDate: LocalDate?) = CaseDto(
    crn = crn,
    userAccess = UserAccess.LIMITED,
    limitedAccess = true,
    teamCode = teamCode,
    assignedTo = assignedTo,
    inUsersCustomList = inUsersCustomList,
    accommodationSummaries =
    toAccommodationSummariesDto(
      accommodationStatus = accommodationStatus,
      accommodationStatusDate = accommodationStatusDate,
      currentAccommodation = null,
      nextAccommodation = null,
    ),
  )
}
