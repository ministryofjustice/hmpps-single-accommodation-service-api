package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation

import java.time.ZonedDateTime

data class ProbationCreateAddress(
  val noFixedAbode: Boolean,
  val typeVerified: Boolean,
  val startDate: ZonedDateTime,
  val endDate: ZonedDateTime?,
  val postcode: String?,
  val uprn: String?,
  val subBuildingName: String?,
  val buildingName: String?,
  val buildingNumber: String?,
  val thoroughfareName: String?,
  val dependentLocality: String?,
  val postTown: String?,
  val county: String?,
  val comment: String?,
  val statusCode: AddressStatusCode,
  val usages: List<AddressUsage>,
  val contacts: List<AddressContact>,
)
