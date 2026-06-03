package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CRS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServicesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val commissionedRehabilitativeServicesCachingService: CommissionedRehabilitativeServicesCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
  val tierCachingService: TierCachingService,
) {

  fun getData(crn: String, prisonNumber: String?): OrchestrationResultDto<EligibilityOrchestrationDto> {
    val calls = buildMap {
      put(GET_CORE_PERSON_RECORD_BY_CRN) { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) }
      put(GET_TIER) { tierCachingService.getTier(crn) }
      put(GET_CAS_1_APPLICATION) { approvedPremisesCachingService.getSuitableCas1Application(crn) }
      put(GET_CAS_3_APPLICATION) { approvedPremisesCachingService.getSuitableCas3Application(crn) }
      put(GET_CRS) { commissionedRehabilitativeServicesCachingService.getCrs(crn) }
      prisonNumber?.let { num -> put(GET_PRISONER) { prisonerSearchCachingService.getPrisoner(num) } }
    }
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    val cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val tier = results.getResult<Tier>(GET_TIER)
    val cas1Application = results.getResult<Cas1Application>(GET_CAS_1_APPLICATION)
    val cas3Application = results.getResult<Cas3Application>(GET_CAS_3_APPLICATION)
    val crs = results.getResult<List<CommissionedRehabilitativeServices>>(GET_CRS)
    val prisoner = results.getResult<Prisoner>(GET_PRISONER)

    return OrchestrationResultDto(
      data = EligibilityOrchestrationDto(
        crn,
        cpr,
        tier,
        cas1Application,
        cas3Application,
        crs,
        prisoner,
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
