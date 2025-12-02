package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord

import java.time.LocalDate

data class CorePersonRecord(
  val cprUUID: String? = null,
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val title: Title? = null,
  val sex: Sex? = null,
  val religion: Religion? = null,
  val ethnicity: Ethnicity? = null,
  val aliases: List<Alias> = emptyList(),
  var nationalities: List<Nationality> = emptyList(),
  val addresses: List<Address> = emptyList(),
  val identifiers: Identifiers? = null,
)

data class Title(
  val code: String? = null,
  val description: String? = null,
)

data class Sex(
  val code: String? = null,
  val description: String? = null,
)

data class Religion(
  val code: String? = null,
  val description: String? = null,
)

data class Ethnicity(
  val code: String? = null,
  val description: String? = null,
)

data class Alias(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleNames: String? = null,
  val title: Title? = null,
  val sex: Sex? = null,
)

data class Nationality(
  val code: String? = null,
  val description: String? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val notes: String? = null,
)

data class Address(
  val noFixedAbode: Boolean? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val uprn: String? = null,
)

data class Identifiers(
  val crns: List<String> = emptyList(),
  val prisonNumbers: List<String> = emptyList(),
  val defendantIds: List<String> = emptyList(),
  val cids: List<String> = emptyList(),
  val pncs: List<String> = emptyList(),
  val cros: List<String> = emptyList(),
  val nationalInsuranceNumbers: List<String> = emptyList(),
  val driverLicenseNumbers: List<String> = emptyList(),
  val arrestSummonsNumbers: List<String> = emptyList(),
)
