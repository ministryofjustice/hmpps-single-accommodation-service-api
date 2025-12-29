package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import java.time.LocalDate
import kotlin.String

fun buildPrisoner(
  prisonerNumber: String? = "123456",
  releaseDate: LocalDate? = LocalDate.now(),
  confirmedReleaseDate: LocalDate? = LocalDate.now(),
) = Prisoner(
  prisonerNumber = prisonerNumber,
  releaseDate = releaseDate,
  confirmedReleaseDate = confirmedReleaseDate,
)
