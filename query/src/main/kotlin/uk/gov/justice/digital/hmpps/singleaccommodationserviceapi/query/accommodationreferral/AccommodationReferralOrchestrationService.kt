package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_DTR_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService

@Service
class AccommodationReferralOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {
  fun fetchAllReferralsAggregated(crn: String): OrchestrationResultDto<AccommodationReferralOrchestrationDto> {
    val calls = mapOf(
      GET_CAS1_REFERRAL to { approvedPremisesCachingService.getCas1Referral(crn) },
      GET_CAS2_REFERRAL to { approvedPremisesCachingService.getCas2Referral(crn) },
      GET_CAS2V2_REFERRAL to { approvedPremisesCachingService.getCas2v2Referral(crn) },
      GET_CAS3_REFERRAL to { approvedPremisesCachingService.getCas3Referral(crn) },
      GET_DTR_REFERRAL to { listOf(dutyToReferQueryService.getDutyToRefer(crn)) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )
    val cas1 =
      results.standardCallsNoIterationResults!!.getResult<List<Cas1ReferralHistory>>(GET_CAS1_REFERRAL)
        ?: emptyList()
    val cas2 =
      results.standardCallsNoIterationResults!!.getResult<List<Cas2ReferralHistory>>(GET_CAS2_REFERRAL)
        ?: emptyList()
    val cas2v2 =
      results.standardCallsNoIterationResults!!.getResult<List<Cas2ReferralHistory>>(GET_CAS2V2_REFERRAL)
        ?: emptyList()
    val cas3 =
      results.standardCallsNoIterationResults!!.getResult<List<Cas3ReferralHistory>>(GET_CAS3_REFERRAL)
        ?: emptyList()
    val dutyToRefer = results.standardCallsNoIterationResults!!.getResult<List<DutyToReferDto>>(GET_DTR_REFERRAL)
      ?: emptyList()

    return OrchestrationResultDto(
      data = AccommodationReferralOrchestrationDto(cas1, cas2, cas2v2, cas3, dutyToRefer),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
