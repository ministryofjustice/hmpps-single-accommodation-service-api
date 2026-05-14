package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val sasAndDeliusCachingService: SasAndDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {
  fun getCaseList(username: String): OrchestrationResultDto<CaseList> {
    val bulkCall = mapOf(
      GET_CASE_LIST to { sasAndDeliusCachingService.getCaseList(username) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
    )
    val caseList = results.standardCallsNoIterationResults
      ?.getResult<CaseList>(GET_CASE_LIST)
      ?: CaseList(emptyList())
    return OrchestrationResultDto(
      data = caseList,
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }

  fun getCase(username: String, crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(username, crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val case = results.standardCallsNoIterationResults!!.getResult<Case>(ApiCallKeys.GET_CASE)
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val roshDetails = results.standardCallsNoIterationResults!!.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)

    return OrchestrationResultDto(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        roshDetails = roshDetails,
        tier = tier,
        case = case,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
