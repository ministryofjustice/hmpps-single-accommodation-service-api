package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto

import kotlin.String
import kotlin.collections.List
import kotlin.collections.sortedByDescending

@Service
class AccommodationReferralService(private val orchestrationService: AccommodationReferralOrchestrationService) {

  fun getReferralHistory(crn: String): List<AccommodationReferralDto> {
    val orchestrationDto = orchestrationService.fetchAllReferralsAggregated(crn)
    val allReferrals = transformReferrals(orchestrationDto)
    return allReferrals.sortedByDescending { it.date }
  }
}
