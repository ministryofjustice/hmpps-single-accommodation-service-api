package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.model.Phone

data class RedisIntegrationResponse(
  val primaryPhone: Phone?,
)