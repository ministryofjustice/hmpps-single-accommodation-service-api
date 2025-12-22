package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CasService
import kotlin.String
import kotlin.collections.List
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.sortedByDescending

@Service
class AccommodationReferralService(private val service: AccommodationReferralOrchestrationService) {

  fun getReferralHistory(crn: String): List<AccommodationReferralDto> {
    val orchestrationDto = service.fetchAllReferralsAggregated(crn)

    val allReferrals =
      orchestrationDto.cas1Referrals.map {
        AccommodationReferralDto(
          id = it.id,
          type = CasService.CAS1,
          status = CasReferralStatus.from(it.status),
          date = it.createdAt,
        )
      } +
        orchestrationDto.cas2Referrals.map {
          AccommodationReferralDto(
            id = it.id,
            type = CasService.CAS2,
            status = CasReferralStatus.from(it.status),
            date = it.createdAt,
          )
        } +
        orchestrationDto.cas2v2Referrals.map {
          AccommodationReferralDto(
            id = it.id,
            type = CasService.CAS2v2,
            status = CasReferralStatus.from(it.status),
            date = it.createdAt,
          )
        } +
        orchestrationDto.cas3Referrals.map {
          AccommodationReferralDto(
            id = it.id,
            type = CasService.CAS3,
            status = CasReferralStatus.from(it.status),
            date = it.createdAt,
          )
        }

    return allReferrals.sortedByDescending { it.date }
  }
}
