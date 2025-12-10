package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch

import java.time.LocalDate

data class Prisoner(
  var prisonerNumber: String? = null,
  var releaseDate: LocalDate? = null,
  var confirmedReleaseDate: LocalDate? = null,
)
