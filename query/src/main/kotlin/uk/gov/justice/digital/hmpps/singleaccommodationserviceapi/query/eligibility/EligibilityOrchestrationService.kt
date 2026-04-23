package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val tierCachingService: TierCachingService,
) {

  fun getData(crn: String): OrchestrationResultDto<EligibilityOrchestrationDto> {
    val calls = mapOf(
      GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordAddressesByCrn(crn) },
      GET_TIER to { tierCachingService.getTier(crn) },
      GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
      GET_CAS_3_APPLICATION to { approvedPremisesCachingService.getSuitableCas3Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val cprAddresses = results.standardCallsNoIterationResults!!.getResult<CorePersonRecordAddresses>(GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN)
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(GET_TIER)
    val cas1Application = results.standardCallsNoIterationResults!!.getResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = results.standardCallsNoIterationResults!!.getResult<Cas3Application>(GET_CAS_3_APPLICATION)

    return OrchestrationResultDto(
      data = EligibilityOrchestrationDto(
        crn,
        cpr,
        cprAddresses,
        tier,
        cas1Application,
        cas3Application,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
