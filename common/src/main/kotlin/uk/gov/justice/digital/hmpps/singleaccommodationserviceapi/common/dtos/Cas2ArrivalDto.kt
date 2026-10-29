package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant

data class Cas2ArrivalDto(
  val markedAsArrivedDateTime: Instant,
)
