package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical

import java.time.LocalDateTime

data class CanonicalAddress(
  val cprAddressId: String,
  val noFixedAbode: Boolean? = null,
  val startDate: String? = null,
  val startDateTime: LocalDateTime? = null,
  val endDate: String? = null,
  val endDateTime: LocalDateTime? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val countryCode: String? = null,
  val uprn: String? = null,
  val status: CanonicalAddressStatus,
  val comment: String? = null,
  val typeVerified: Boolean? = null,
  val usages: List<CanonicalAddressUsage> = emptyList(),
  val contacts: List<CanonicalContact> = emptyList(),
)
