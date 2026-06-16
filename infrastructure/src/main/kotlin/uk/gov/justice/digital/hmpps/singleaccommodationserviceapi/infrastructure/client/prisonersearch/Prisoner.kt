package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch

import java.time.LocalDate

data class Prisoner(
  val prisonerNumber: String? = null,
  val releaseDate: LocalDate? = null,
  val confirmedReleaseDate: LocalDate? = null,
  val inOutStatus: InOutStatus? = null,
  val prisonId: String? = null,
  val prisonName: String? = null,
  val status: String? = null,
)

enum class InOutStatus {
  IN,
  OUT,
  TRN,
}
