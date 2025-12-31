package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildCorePersonRecord(
  cprUUID: UUID = UUID.randomUUID(),
  identifiers: Identifiers? = buildIdentifiers(),
  firstName: String = "First",
  middleNames: String = "Middle",
  lastName: String = "Last",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  sex: Sex = buildSex(),
) = CorePersonRecord(
  cprUUID = cprUUID,
  identifiers = identifiers,
  firstName = firstName,
  lastName = lastName,
  middleNames = middleNames,
  dateOfBirth = dateOfBirth,
  sex = sex,
)

fun buildSex(
  code: SexCode = SexCode.F,
) = Sex(
  code = code,
  description = if (code == SexCode.F) "Female" else "Male",
)

fun buildIdentifiers(
  crns: List<String> = listOf("XX12345X"),
  prisonNumbers: List<String> = listOf("PRI1"),
  pncs: List<String> = listOf("Some PNC Reference"),
) = Identifiers(crns = crns, prisonNumbers = prisonNumbers, pncs = pncs)
