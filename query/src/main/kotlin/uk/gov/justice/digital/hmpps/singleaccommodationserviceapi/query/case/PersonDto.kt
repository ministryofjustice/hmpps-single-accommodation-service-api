package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PersonNamesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import java.time.LocalDate

sealed interface PersonDto {
  val crn: String
  val nomsNumber: String?
  val teamCode: String
  val assignedTo: AssignedToDto
}

sealed interface Identifiable {
  val name: String
  val personNames: PersonNamesDto?
  val dateOfBirth: LocalDate?
  val pncNumber: String?
  val gender: String
  val riskLevel: RiskLevel?
}

data class LimitedPersonDto(
  override val crn: String,
  override val nomsNumber: String?,
  override val teamCode: String,
  override val assignedTo: AssignedToDto,
) : PersonDto

data class FullPersonDto(
  override val crn: String,
  override val nomsNumber: String?,
  override val teamCode: String,
  override val assignedTo: AssignedToDto,
  override val riskLevel: RiskLevel?,
  override val name: String,
  override val personNames: PersonNamesDto?,
  override val pncNumber: String?,
  override val dateOfBirth: LocalDate,
  override val gender: String,
  val limitedAccess: Boolean,
) : PersonDto,
  Identifiable
