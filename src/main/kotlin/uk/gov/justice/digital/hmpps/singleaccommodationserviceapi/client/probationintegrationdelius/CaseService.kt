package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusClient

@Service
class CaseService(
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
) {
  fun getCases(crns: List<String>): CaseSummaries = probationIntegrationDeliusClient.postCaseSummaries(crns)
}
