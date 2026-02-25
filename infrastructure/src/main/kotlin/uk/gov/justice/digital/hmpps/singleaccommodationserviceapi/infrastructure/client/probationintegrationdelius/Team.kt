package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius

import java.time.LocalDate

data class Team(
  val code: String,
  val name: String,
  val ldu: Ldu,
  val borough: Borough?,
  val startDate: LocalDate,
  val endDate: LocalDate?,
)

data class Borough(val code: String, val description: String)

data class Ldu(val code: String, val name: String)
