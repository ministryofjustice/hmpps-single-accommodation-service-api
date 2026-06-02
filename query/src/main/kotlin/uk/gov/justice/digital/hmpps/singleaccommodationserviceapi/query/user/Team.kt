package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import java.time.LocalDate

data class Team(
  val code: String,
  val name: String,
  val ldu: Ldu? = null,
  val borough: Borough? = null,
  val startDate: LocalDate,
  val endDate: LocalDate? = null,
)

data class Ldu(
  val code: String,
  val name: String,
)

data class Borough(
  val code: String,
  val description: String,
)
