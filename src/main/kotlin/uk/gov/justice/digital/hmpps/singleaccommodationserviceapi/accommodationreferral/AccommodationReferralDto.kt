package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.TemporaryAccommodationAssessmentStatus
import java.time.Instant
import java.util.UUID

data class AccommodationReferralDto(
  val id: UUID,
  val type: CasType,
  val status: CasReferralStatus,
  val date: Instant,
) {
  constructor(casReferral: Cas1ReferralHistory) : this(
    casReferral.id,
    CasType.CAS1,
    CasReferralStatus.from(casReferral.status),
    casReferral.createdAt,
  )

  constructor(casReferral: Cas2ReferralHistory) : this(
    casReferral.id,
    CasType.CAS2,
    CasReferralStatus.fromCas2(casReferral.status),
    casReferral.createdAt,
  )

  constructor(casReferral: Cas2v2ReferralHistory) : this(
    casReferral.id,
    CasType.CAS2v2,
    CasReferralStatus.fromCas2(casReferral.status),
    casReferral.createdAt,
  )

  constructor(casReferral: Cas3ReferralHistory) : this(
    casReferral.id,
    CasType.CAS3,
    CasReferralStatus.from(casReferral.status),
    casReferral.createdAt,
  )
}

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

    fun from(status: TemporaryAccommodationAssessmentStatus): CasReferralStatus = when (status) {
      TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> ACCEPTED
      TemporaryAccommodationAssessmentStatus.CLOSED,
      TemporaryAccommodationAssessmentStatus.REJECTED,
      -> REJECTED
      TemporaryAccommodationAssessmentStatus.UNALLOCATED,
      TemporaryAccommodationAssessmentStatus.IN_REVIEW,
      -> PENDING
    }

    private val cas2Map = mapOf(
      "More information requested" to PENDING,
      "Place offered" to ACCEPTED,
      "Awaiting arrival" to PENDING,
      "Referral cancelled" to REJECTED,
      "Referral withdrawn" to REJECTED,
      "Offer accepted" to ACCEPTED,
      "On waiting list" to PENDING,
      "Awaiting decision" to PENDING,
      "Offer declined or withdrawn" to REJECTED,
    )

    fun fromCas2(status: String): CasReferralStatus = cas2Map[status] ?: throw IllegalArgumentException("Unknown CAS2 referral status: $status")
  }
}

enum class CasType {
  CAS1,
  CAS2,
  CAS2v2,
  CAS3,
}
