package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class CanonicalAddressUsage(
  @field:JsonUnwrapped
  val usageCode: CanonicalAddressUsageCode,
  @get:JsonProperty("isActive")
  val isActive: Boolean,
)
