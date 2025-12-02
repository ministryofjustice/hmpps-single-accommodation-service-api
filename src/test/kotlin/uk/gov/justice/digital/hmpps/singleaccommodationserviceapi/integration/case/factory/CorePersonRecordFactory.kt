package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Identifiers
import java.time.LocalDate
import java.util.*

fun buildCorePersonRecord(
  cprUUID: UUID = UUID.randomUUID(),
  crn: String = "XX12345X",
  prisonNumber: String = "PRI1",
  firstName: String = "First",
  middleNames: String = "Middle",
  lastName: String = "Last",
  dateOfBirth: LocalDate = LocalDate.now().minusYears(25),
) = CorePersonRecord(
  cprUUID = cprUUID,
  identifiers = Identifiers(crns = listOf(crn), prisonNumbers = listOf(prisonNumber)),
  firstName = firstName,
  lastName = lastName,
  middleNames = middleNames,
  dateOfBirth = dateOfBirth,
)
