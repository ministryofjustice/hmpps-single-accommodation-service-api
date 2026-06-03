package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import kotlin.String
import kotlin.collections.List
import kotlin.collections.sortedByDescending

@Service
class AccommodationReferralService(private val userService: UserService, private val orchestrationService: AccommodationReferralOrchestrationService) {

  fun getReferralHistory(crn: String): ApiResponseDto<List<AccommodationReferralDto>> {
    val orchestrationDto = orchestrationService.fetchAllReferralsAggregated(crn)

    val usernames = (
      orchestrationDto.data.cas1Referrals.map { it.referredBy?.username } +
        orchestrationDto.data.cas2Referrals.map { it.referredBy?.username } +
        orchestrationDto.data.cas2v2Referrals.map { it.referredBy?.username } +
        orchestrationDto.data.cas3Referrals.map { it.referredBy?.username }
      ).filterNotNull().distinct()

    val usersMap = usernames.map { userService.getExistingDeliusUserOrCreate(Username(it)) }.associateBy { it.username }

    val allReferrals = AccommodationReferralTransformer.transformReferrals(orchestrationDto.data, usersMap).sortedByDescending { it.date }
    return toApiResponseDto(
      data = allReferrals,
      upstreamFailures = orchestrationDto.upstreamFailures,
    )
  }
}
