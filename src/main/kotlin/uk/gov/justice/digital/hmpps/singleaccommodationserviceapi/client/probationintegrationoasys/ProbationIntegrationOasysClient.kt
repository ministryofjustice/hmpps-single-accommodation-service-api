package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys

import org.springframework.web.service.annotation.GetExchange

interface ProbationIntegrationOasysClient {

  @GetExchange(value = "/info")
  fun getInfo(): String
}