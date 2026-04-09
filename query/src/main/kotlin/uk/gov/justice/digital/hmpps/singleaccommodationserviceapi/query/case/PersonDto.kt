package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CodeDescription
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Officer
import java.time.LocalDate

class PersonDto(
  val crn: String,
  val name: Name,
  val nomsNumber: String?,
  val pncNumber: String?,
  val dateOfBirth: LocalDate,
  val staff: Officer,
  val gender: String,
  val roshLevel: CodeDescription?,
  val expectedReleaseDate: LocalDate?,
)
