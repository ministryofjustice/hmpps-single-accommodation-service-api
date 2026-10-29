package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.time.Instant

data class Cas2Arrival(
  val markedAsArrivedDateTime: Instant,
  val arrivedByUsername: String,
)
