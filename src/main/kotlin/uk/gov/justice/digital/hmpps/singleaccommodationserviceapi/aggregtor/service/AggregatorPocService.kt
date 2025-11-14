package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.client.PhoneClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.RedisIntegrationResponse

@Service
class AggregatorPocService(
  private val phoneClient: PhoneClient
) {

  fun orchestrateCallsThatWriteToCache(): RedisIntegrationResponse {
    val id = 1L
    return RedisIntegrationResponse(
      primaryPhone = phoneClient.getPhoneById(id),
    )
  }
}