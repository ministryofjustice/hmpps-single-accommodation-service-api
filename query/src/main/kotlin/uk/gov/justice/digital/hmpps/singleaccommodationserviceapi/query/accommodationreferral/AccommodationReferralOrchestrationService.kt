package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.OrchestrationResultDto

@Service
class AccommodationReferralOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  fun fetchAllReferralsAggregated(crn: String): OrchestrationResultDto<AccommodationReferralOrchestrationDto> {
    val calls = mapOf(
      GET_CAS1_REFERRAL to { approvedPremisesCachingService.getCas1Referral(crn) },
      GET_CAS2_REFERRAL to { approvedPremisesCachingService.getCas2Referral(crn) },
      GET_CAS2V2_REFERRAL to { approvedPremisesCachingService.getCas2v2Referral(crn) },
      GET_CAS3_REFERRAL to { approvedPremisesCachingService.getCas3Referral(crn) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val stdResults = results.standardCallsNoIterationResults
    val cas1 =
      stdResults?.getResult<List<ReferralHistory<Cas1AssessmentStatus>>>(GET_CAS1_REFERRAL)
        ?: emptyList()
    val cas2 =
      stdResults?.getResult<List<ReferralHistory<Cas2Status>>>(GET_CAS2_REFERRAL)
        ?: emptyList()
    val cas2v2 =
      stdResults?.getResult<List<ReferralHistory<Cas2Status>>>(GET_CAS2V2_REFERRAL)
        ?: emptyList()
    val cas3 =
      stdResults?.getResult<List<ReferralHistory<TemporaryAccommodationAssessmentStatus>>>(GET_CAS3_REFERRAL)
        ?: emptyList()

    val failures = stdResults?.getFailures() ?: emptyList()

    return OrchestrationResultDto(
      data = AccommodationReferralOrchestrationDto(
        cas1,
        cas2,
        cas2v2,
        cas3,
      ),
      upstreamFailures = failures,
    )
  }
}
