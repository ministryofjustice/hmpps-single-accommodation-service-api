package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import org.springframework.web.service.annotation.GetExchange

interface ApprovedPremisesClient {

  @GetExchange(value = "/info")
  fun getInfo(): String
}
