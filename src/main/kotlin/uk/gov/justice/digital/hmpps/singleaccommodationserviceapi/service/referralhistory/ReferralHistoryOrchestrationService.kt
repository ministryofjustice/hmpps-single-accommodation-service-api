package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.referralhistory

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService

@Component
class ReferralHistoryOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun fetchAllReferralsAggregated(crn: String): List<Referral> {

    val calls = mapOf(
      GET_CAS1_REFERRAL to { approvedPremisesCachingService.getCas1Referral(crn) },
      GET_CAS2_REFERRAL to { approvedPremisesCachingService.getCas2Referral(crn) },
      GET_CAS2V2_REFERRAL to { approvedPremisesCachingService.getCas2v2Referral(crn) },
      GET_CAS3_REFERRAL to { approvedPremisesCachingService.getCas3Referral(crn) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cas1 = results.standardCallsNoIterationResults!![GET_CAS1_REFERRAL] as? List<Referral>
      ?: error("${GET_CAS1_REFERRAL} failed for $crn")
    val cas2 = results.standardCallsNoIterationResults!![GET_CAS2_REFERRAL] as? List<Referral>
      ?: error("${GET_CAS2_REFERRAL} failed for $crn")
    val cas2v2 = results.standardCallsNoIterationResults!![GET_CAS2V2_REFERRAL] as? List<Referral>
      ?: error("${GET_CAS2V2_REFERRAL} failed for $crn")
    val cas3 = results.standardCallsNoIterationResults!![GET_CAS3_REFERRAL] as? List<Referral>
      ?: error("${GET_CAS3_REFERRAL} failed for $crn")

    val allReferrals = cas1 + cas2 + cas2v2 + cas3

    return allReferrals.sortedByDescending { it.date }
  }
}
