package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import java.time.Instant
import java.util.UUID

object AccommodationReferralTransformer {
  fun transformReferrals(dto: AccommodationReferralOrchestrationDto) = dto.cas1Referrals.map {
    toAccommodationReferralDto(
      id = it.id,
      type = CasService.CAS1,
      status = toCasReferralStatus(it.status),
      date = it.createdAt,
    )
  } +
    dto.cas2Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = CasService.CAS2,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
      )
    } +
    dto.cas2v2Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = CasService.CAS2v2,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
      )
    } +
    dto.cas3Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = CasService.CAS3,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
      )
    }

  fun toAccommodationReferralDto(
    id: UUID,
    type: CasService,
    status: CasReferralStatus,
    date: Instant,
  ) = AccommodationReferralDto(
    id = id,
    type = type,
    status = status,
    date = date,
  )

  fun toCasReferralStatus(status: Cas1AssessmentStatus): CasReferralStatus = when (status) {
    Cas1AssessmentStatus.COMPLETED -> CasReferralStatus.ACCEPTED
    Cas1AssessmentStatus.REALLOCATED -> CasReferralStatus.REJECTED
    Cas1AssessmentStatus.AWAITING_RESPONSE,
    Cas1AssessmentStatus.IN_PROGRESS,
    Cas1AssessmentStatus.NOT_STARTED,
    -> CasReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: Cas2Status): CasReferralStatus = when (status) {
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

  fun toCasReferralStatus(status: TemporaryAccommodationAssessmentStatus): CasReferralStatus = when (status) {
    TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> CasReferralStatus.ACCEPTED
    TemporaryAccommodationAssessmentStatus.CLOSED,
    TemporaryAccommodationAssessmentStatus.REJECTED,
    -> CasReferralStatus.REJECTED

    TemporaryAccommodationAssessmentStatus.UNALLOCATED,
    TemporaryAccommodationAssessmentStatus.IN_REVIEW,
    -> CasReferralStatus.PENDING
  }
}
