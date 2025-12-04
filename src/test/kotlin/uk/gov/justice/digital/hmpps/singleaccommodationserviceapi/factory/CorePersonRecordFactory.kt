package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Identifiers
import java.time.LocalDate
import java.util.UUID

fun buildCorePersonRecord(
  cprUUID: UUID = UUID.randomUUID(),
  identifiers: Identifiers? = buildIdentifiers(),
  firstName: String = "First",
  middleNames: String = "Middle",
  lastName: String = "Last",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
) = CorePersonRecord(
  cprUUID = cprUUID,
  identifiers = identifiers,
  firstName = firstName,
  lastName = lastName,
  middleNames = middleNames,
  dateOfBirth = dateOfBirth,
)

fun buildIdentifiers(
  crns: List<String> = listOf("XX12345X"),
  prisonNumbers: List<String> = listOf("PRI1"),
  pncs: List<String> = listOf("Some PNC Reference"),
) = Identifiers(crns = crns, prisonNumbers = prisonNumbers, pncs = pncs)
