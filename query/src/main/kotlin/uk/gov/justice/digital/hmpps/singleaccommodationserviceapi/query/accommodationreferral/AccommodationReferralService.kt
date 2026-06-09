package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import kotlin.String
import kotlin.collections.List
import kotlin.collections.sortedByDescending

@Service
class AccommodationReferralService(
  private val orchestrationService: AccommodationReferralOrchestrationService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {

  fun getReferralHistory(crn: String): ApiResponseDto<List<AccommodationReferralDto>> {
    val orchestrationDto = orchestrationService.fetchAllReferralsAggregated(crn)

    val dtrs = dutyToReferQueryService.getDutyToReferHistory(crn)

    val allReferrals = AccommodationReferralTransformer.transformReferrals(orchestrationDto.data, dtrs).sortedByDescending { it.date }
    return toApiResponseDto(
      data = allReferrals,
      upstreamFailures = orchestrationDto.upstreamFailures,
    )
  }
}
