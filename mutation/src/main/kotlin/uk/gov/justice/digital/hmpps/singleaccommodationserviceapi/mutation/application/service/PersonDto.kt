package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CodeDescription
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.IndividualName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Officer
import java.time.LocalDate

class PersonDto(
  val crn: String,
  val name: IndividualName,
  val nomsNumber: String?,
  val pncNumber: String?,
  val dateOfBirth: LocalDate,
  val staff: Officer,
  val team: CodeDescription,
  val gender: String,
  val roshLevel: CodeDescription?,
  val expectedReleaseDate: LocalDate?,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String?,
  val restrictionMessage: String?,
)
