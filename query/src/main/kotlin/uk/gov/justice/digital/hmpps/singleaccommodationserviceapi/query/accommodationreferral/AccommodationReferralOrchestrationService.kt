package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus

@Service
class AccommodationReferralOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
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

    val cas1 =
      results.standardCallsNoIterationResults?.get(GET_CAS1_REFERRAL) as? List<ReferralHistory<Cas1AssessmentStatus>>
        ?: emptyList()
    val cas2 =
      results.standardCallsNoIterationResults?.get(GET_CAS2_REFERRAL) as? List<ReferralHistory<Cas2Status>>
        ?: emptyList()
    val cas2v2 =
      results.standardCallsNoIterationResults?.get(GET_CAS2V2_REFERRAL) as? List<ReferralHistory<Cas2Status>>
        ?: emptyList()
    val cas3 =
      results.standardCallsNoIterationResults?.get(GET_CAS3_REFERRAL) as? List<ReferralHistory<TemporaryAccommodationAssessmentStatus>>
        ?: emptyList()

    return AccommodationReferralOrchestrationDto(cas1, cas2, cas2v2, cas3)
  }
}
