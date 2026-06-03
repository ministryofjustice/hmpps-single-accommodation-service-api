package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import java.time.LocalDate

fun buildPrisoner(
  prisonerNumber: String? = "PRI1",
  releaseDate: LocalDate? = LocalDate.now(),
  confirmedReleaseDate: LocalDate? = LocalDate.now(),
) = Prisoner(
  prisonerNumber = prisonerNumber,
  releaseDate = releaseDate,
  confirmedReleaseDate = confirmedReleaseDate,
)
