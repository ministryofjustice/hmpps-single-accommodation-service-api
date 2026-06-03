package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch

import java.time.LocalDate

data class Prisoner(
  var prisonerNumber: String? = null,
  var releaseDate: LocalDate? = null,
  var confirmedReleaseDate: LocalDate? = null,
  var inOutStatus: InOutStatus? = null,
  var prisonId: String? = null,
  var prisonName: String? = null,
  var status: String? = null,
)

enum class InOutStatus {
  IN,
  OUT,
  TRN,
}
