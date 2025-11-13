package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model

data class Cas1PremisesBasicSummary(
  val id: java.util.UUID,
  val name: String,
  val apArea: NamedId,
  val bedCount: Int,
  val supportsSpaceBookings: Boolean,
  val fullAddress: String,
  val postcode: String,
  val apCode: String? = null,
)
