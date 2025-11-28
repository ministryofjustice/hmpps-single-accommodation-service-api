package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import java.time.LocalDate

data class DomainData(
  val tier: String,
  val sex: Sex,
  val referralDate: LocalDate?,
  val releaseDate: LocalDate,
)
