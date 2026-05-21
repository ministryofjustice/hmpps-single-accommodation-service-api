package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical

data class CanonicalContact(
  val type: CanonicalContactType,
  val value: String? = null,
  val extension: String? = null,
)
