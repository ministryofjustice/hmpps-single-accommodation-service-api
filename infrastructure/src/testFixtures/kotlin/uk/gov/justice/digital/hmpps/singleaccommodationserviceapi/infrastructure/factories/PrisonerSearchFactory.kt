package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import java.time.LocalDate

fun buildPrisoner(
  prisonNumber: String? = "PRI1",
  releaseDate: LocalDate? = LocalDate.now(),
  confirmedReleaseDate: LocalDate? = LocalDate.now(),
  inOutStatus: InOutStatus? = null,
  prisonId: String? = null,
  prisonName: String? = null,
  status: String? = null,
) = Prisoner(
  prisonerNumber = prisonNumber,
  releaseDate = releaseDate,
  confirmedReleaseDate = confirmedReleaseDate,
  inOutStatus = inOutStatus,
  prisonId = prisonId,
  prisonName = prisonName,
  status = status,
)
