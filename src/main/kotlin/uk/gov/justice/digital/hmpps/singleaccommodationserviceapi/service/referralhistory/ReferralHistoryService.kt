package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.referralhistory

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral

@Service
class ReferralHistoryService(private val service: ReferralHistoryOrchestrationService) {

  fun getReferralHistory(crn: String): List<Referral> {
    return service.fetchAllReferralsAggregated(crn)
  }
}