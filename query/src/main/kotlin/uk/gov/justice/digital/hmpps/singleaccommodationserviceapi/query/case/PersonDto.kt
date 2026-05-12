package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
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
  val dateOfBirth: LocalDate?
  val pncNumber: String?
  val gender: String
  val roshLevel: RiskLevel?
}

data class ExcludedPersonDto(
  override val crn: String,
  override val nomsNumber: String?,
  override val teamCode: String,
  override val assignedTo: AssignedToDto,
) : PersonDto

data class RestrictedPersonDto(
  override val crn: String,
  override val nomsNumber: String?,
  override val teamCode: String,
  override val assignedTo: AssignedToDto,
  override val roshLevel: RiskLevel?,
  override val name: String,
  override val pncNumber: String?,
  override val dateOfBirth: LocalDate,
  override val gender: String,

) : PersonDto,
  Identifiable

data class FullPersonDto(
  override val crn: String,
  override val nomsNumber: String?,
  override val teamCode: String,
  override val assignedTo: AssignedToDto,
  override val roshLevel: RiskLevel?,
  override val name: String,
  override val pncNumber: String?,
  override val dateOfBirth: LocalDate,
  override val gender: String,
) : PersonDto,
  Identifiable
