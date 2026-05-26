package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.domain

data class Name(
  val forename: String,
  val middleName: String?,
  val surname: String,
) {
  val fullName: String
    get() = listOfNotNull(
      forename,
      middleName?.takeIf { it.isNotBlank() },
      surname,
    ).joinToString(" ")
}
