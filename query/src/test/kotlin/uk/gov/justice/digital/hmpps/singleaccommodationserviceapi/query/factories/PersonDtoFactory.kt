package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CodeDescription
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Officer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshCodeDescription
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import java.time.LocalDate

fun buildPersonDto(
  crn: String,
  name: Name = buildName(),
  nomsNumber: String = "PRI1",
  pncNumber: String = "Some PNC Reference",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  staff: Officer = buildOfficer(),
  gender: String = "Male",
  roshLevel: CodeDescription = buildRoshCodeDescription(),
) = PersonDto(
  crn,
  name,
  nomsNumber,
  pncNumber,
  dateOfBirth,
  staff,
  gender,
  roshLevel,
)
