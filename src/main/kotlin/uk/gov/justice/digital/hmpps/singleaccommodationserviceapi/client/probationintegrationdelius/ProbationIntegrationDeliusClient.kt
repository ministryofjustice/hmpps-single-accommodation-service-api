package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case.CaseSummaries

interface ProbationIntegrationDeliusClient {

  @GetExchange(value = "/info")
  fun getInfo(): String

  @GetExchange(value = "/probation-cases/summaries")
  fun getCaseSummaries(@RequestBody crns: List<String>): CaseSummaries

  @PostExchange(value = "/probation-cases/summaries")
  fun postCaseSummaries(@RequestBody crns: List<String>): CaseSummaries
}
