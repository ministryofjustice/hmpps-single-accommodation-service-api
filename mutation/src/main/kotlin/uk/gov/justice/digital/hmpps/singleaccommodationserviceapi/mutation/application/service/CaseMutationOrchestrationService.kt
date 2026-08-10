package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient

@Service
class CaseMutationOrchestrationService(
  val aggregatorService: AggregatorService,
  val tierClient: TierClient,
  val approvedPremisesClient: ApprovedPremisesClient,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun getCase(crn: String): CaseMutationOrchestrationDto {
    val calls = mapOf(
      ApiCallKeys.GET_TIER to { tierClient.getTier(crn) },
      ApiCallKeys.GET_CAS_1_APPLICATION to { approvedPremisesClient.getSuitableCas1ApplicationInternal(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)
    val cas1Application = results.standardCallsNoIterationResults!!.getResult<Cas1Application>(ApiCallKeys.GET_CAS_1_APPLICATION)
    return CaseMutationOrchestrationDto(crn, cpr = null, tier, cas1Application)
  }
}
