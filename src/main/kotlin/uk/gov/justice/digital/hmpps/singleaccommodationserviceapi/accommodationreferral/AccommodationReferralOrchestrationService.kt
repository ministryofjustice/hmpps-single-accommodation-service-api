package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas3ReferralHistory

@Service
class AccommodationReferralOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun fetchAllReferralsAggregated(crn: String): AccommodationReferralOrchestrationDto {
    val calls = mapOf(
      GET_CAS1_REFERRAL to { approvedPremisesCachingService.getCas1Referral(crn) },
      GET_CAS2_REFERRAL to { approvedPremisesCachingService.getCas2Referral(crn) },
      GET_CAS2V2_REFERRAL to { approvedPremisesCachingService.getCas2v2Referral(crn) },
      GET_CAS3_REFERRAL to { approvedPremisesCachingService.getCas3Referral(crn) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cas1 = results.standardCallsNoIterationResults!![GET_CAS1_REFERRAL] as? List<Cas1ReferralHistory>
      ?: error("${GET_CAS1_REFERRAL} failed for $crn")
    val cas2 = results.standardCallsNoIterationResults!![GET_CAS2_REFERRAL] as? List<Cas2ReferralHistory>
      ?: error("${GET_CAS2_REFERRAL} failed for $crn")
    val cas2v2 = results.standardCallsNoIterationResults!![GET_CAS2V2_REFERRAL] as? List<Cas2v2ReferralHistory>
      ?: error("${GET_CAS2V2_REFERRAL} failed for $crn")
    val cas3 = results.standardCallsNoIterationResults!![GET_CAS3_REFERRAL] as? List<Cas3ReferralHistory>
      ?: error("${GET_CAS3_REFERRAL} failed for $crn")

    return AccommodationReferralOrchestrationDto(cas1, cas2, cas2v2, cas3)
  }
}
