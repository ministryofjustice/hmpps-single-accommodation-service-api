package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import org.springframework.stereotype.Service

@Service
class AccommodationReferralService(private val service: AccommodationReferralOrchestrationService) {

  fun getReferralHistory(crn: String): List<AccommodationReferralDto> {
    val orchestrationDto = service.fetchAllReferralsAggregated(crn)

    val allReferrals =
      orchestrationDto.cas1Referrals.map { AccommodationReferralDto(it) } +
        orchestrationDto.cas2Referrals.map { AccommodationReferralDto(it) } +
        orchestrationDto.cas2v2Referrals.map { AccommodationReferralDto(it) } +
        orchestrationDto.cas3Referrals.map { AccommodationReferralDto(it) }
    return allReferrals.sortedByDescending { it.date }
  }
}
