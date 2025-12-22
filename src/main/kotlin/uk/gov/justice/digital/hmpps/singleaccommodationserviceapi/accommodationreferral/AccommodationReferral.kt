package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import java.time.Instant
import java.util.UUID

enum class CasReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
  ;

  // TODO these status mappings need to be confirmed
  companion object {
    fun from(status: Cas1AssessmentStatus): CasReferralStatus = when (status) {
      Cas1AssessmentStatus.COMPLETED -> ACCEPTED

      Cas1AssessmentStatus.REALLOCATED -> REJECTED

      Cas1AssessmentStatus.AWAITING_RESPONSE,
      Cas1AssessmentStatus.IN_PROGRESS,
      Cas1AssessmentStatus.NOT_STARTED,
      -> PENDING
    }

    fun from(status: Cas2Status): CasReferralStatus = when (status) {
      Cas2Status.PLACE_OFFERED,
      Cas2Status.OFFER_ACCEPTED,
      -> ACCEPTED

      Cas2Status.OFFER_DECLINED_OR_WITHDRAWN,
      Cas2Status.REFERRAL_CANCELLED,
      Cas2Status.REFERRAL_WITHDRAWN,
      -> REJECTED

      Cas2Status.MORE_INFORMATION_REQUESTED,
      Cas2Status.AWAITING_ARRIVAL,
      Cas2Status.ON_WAITING_LIST,
      Cas2Status.AWAITING_DECISION,
      -> PENDING
    }

    fun from(status: TemporaryAccommodationAssessmentStatus): CasReferralStatus = when (status) {
      TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> ACCEPTED

      TemporaryAccommodationAssessmentStatus.CLOSED,
      TemporaryAccommodationAssessmentStatus.REJECTED,
      -> REJECTED

      TemporaryAccommodationAssessmentStatus.UNALLOCATED,
      TemporaryAccommodationAssessmentStatus.IN_REVIEW,
      -> PENDING
    }
  }
}

data class AccommodationReferralDto(
  val id: UUID,
  val type: CasService,
  val status: CasReferralStatus,
  val date: Instant,
)
