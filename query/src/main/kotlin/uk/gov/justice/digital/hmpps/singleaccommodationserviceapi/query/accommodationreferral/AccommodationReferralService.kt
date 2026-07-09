package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus.ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus.PENDING
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService.DTR
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationReferralService(
  private val orchestrationService: AccommodationReferralOrchestrationService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {

  fun getReferralHistory(crn: String): ApiResponseDto<List<AccommodationReferralDto>> {
    val orchestrationDto = orchestrationService.fetchAllReferralsAggregated(crn)

    val dtrs = dutyToReferQueryService.getDutyToReferHistory(crn)

    val allReferrals = AccommodationReferralTransformer.transformReferrals(orchestrationDto.data, dtrs)
      .filterNot {
        when (it.type) {
          DTR -> it.status == PENDING
          else -> it.status == PENDING || it.status == ACCEPTED
        }
      }
      .sortedByDescending { it.date }
    return toApiResponseDto(
      data = allReferrals,
      upstreamFailures = orchestrationDto.upstreamFailures,
    )
  }
}
