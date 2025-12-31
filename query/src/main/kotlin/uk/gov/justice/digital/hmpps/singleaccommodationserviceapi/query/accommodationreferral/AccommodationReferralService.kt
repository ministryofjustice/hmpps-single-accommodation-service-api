package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus

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

  fun CasReferralStatus.Companion.from(status: Cas1AssessmentStatus): CasReferralStatus = when (status) {
    Cas1AssessmentStatus.COMPLETED -> CasReferralStatus.ACCEPTED

    Cas1AssessmentStatus.REALLOCATED -> CasReferralStatus.REJECTED

    Cas1AssessmentStatus.AWAITING_RESPONSE,
    Cas1AssessmentStatus.IN_PROGRESS,
    Cas1AssessmentStatus.NOT_STARTED,
      -> CasReferralStatus.PENDING
  }

  fun CasReferralStatus.Companion.from(status: Cas2Status): CasReferralStatus = when (status) {
    Cas2Status.PLACE_OFFERED,
    Cas2Status.OFFER_ACCEPTED,
      -> CasReferralStatus.ACCEPTED

    Cas2Status.OFFER_DECLINED_OR_WITHDRAWN,
    Cas2Status.REFERRAL_CANCELLED,
    Cas2Status.REFERRAL_WITHDRAWN,
      -> CasReferralStatus.REJECTED

    Cas2Status.MORE_INFORMATION_REQUESTED,
    Cas2Status.AWAITING_ARRIVAL,
    Cas2Status.ON_WAITING_LIST,
    Cas2Status.AWAITING_DECISION,
      -> CasReferralStatus.PENDING
  }

  fun CasReferralStatus.Companion.from(status: TemporaryAccommodationAssessmentStatus): CasReferralStatus = when (status) {
    TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> CasReferralStatus.ACCEPTED

    TemporaryAccommodationAssessmentStatus.CLOSED,
    TemporaryAccommodationAssessmentStatus.REJECTED,
      -> CasReferralStatus.REJECTED

    TemporaryAccommodationAssessmentStatus.UNALLOCATED,
    TemporaryAccommodationAssessmentStatus.IN_REVIEW,
      -> CasReferralStatus.PENDING
  }
}
