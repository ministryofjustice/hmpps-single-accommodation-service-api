package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.TemporaryAccommodationAssessmentStatus
import java.time.Instant
import java.util.UUID

data class Referral(
  val id: UUID,
  val type: CasType,
  val status: CasReferralStatus,
  val date: Instant,
)

enum class CasReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
}

enum class CasType {
  CAS1,
  CAS2,
  CAS2v2,
  CAS3,
}

// TODO these status mappings need to be confirmed

fun toReferral(casReferral: Cas1ReferralHistory): Referral =
  Referral(
    casReferral.id,
    CasType.CAS1,
    status = when(casReferral.status) {
      Cas1AssessmentStatus.awaitingResponse -> CasReferralStatus.PENDING
      Cas1AssessmentStatus.completed -> CasReferralStatus.ACCEPTED
      Cas1AssessmentStatus.reallocated -> CasReferralStatus.REJECTED
      Cas1AssessmentStatus.inProgress -> CasReferralStatus.PENDING
      Cas1AssessmentStatus.notStarted -> CasReferralStatus.PENDING
    },
    date = casReferral.createdAt
  )

fun toReferral(casReferral: Cas2ReferralHistory): Referral =
  Referral(
    casReferral.id,
    CasType.CAS2,
    status = when(casReferral.status) {
      "More information requested" -> CasReferralStatus.PENDING
      "Place offered" -> CasReferralStatus.ACCEPTED
      "Awaiting arrival" -> CasReferralStatus.PENDING
      "Referral cancelled" -> CasReferralStatus.REJECTED
      "Referral withdrawn" -> CasReferralStatus.REJECTED
      "Offer accepted" -> CasReferralStatus.ACCEPTED
      "On waiting list" -> CasReferralStatus.PENDING
      "Awaiting decision" -> CasReferralStatus.PENDING
      "Offer declined or withdrawn" -> CasReferralStatus.REJECTED
      else -> throw IllegalArgumentException("Unknown CAS2 referral status: ${casReferral.status}")
    },
    date = casReferral.createdAt
  )


fun toReferral(casReferral: Cas2v2ReferralHistory): Referral =
  Referral(
    casReferral.id,
    CasType.CAS2v2,
    status = when(casReferral.status) {
      "More information requested" -> CasReferralStatus.PENDING
      "Place offered" -> CasReferralStatus.ACCEPTED
      "Awaiting arrival" -> CasReferralStatus.PENDING
      "Referral cancelled" -> CasReferralStatus.REJECTED
      "Referral withdrawn" -> CasReferralStatus.REJECTED
      "Offer accepted" -> CasReferralStatus.ACCEPTED
      "On waiting list" -> CasReferralStatus.PENDING
      "Awaiting decision" -> CasReferralStatus.PENDING
      "Offer declined or withdrawn" -> CasReferralStatus.REJECTED
      else -> throw IllegalArgumentException("Unknown CAS2 referral status: ${casReferral.status}")
    },
    date = casReferral.createdAt
  )

fun toReferral(casReferral: Cas3ReferralHistory): Referral =
  Referral(
    casReferral.id,
    CasType.CAS3,
    status = when(casReferral.status) {
      TemporaryAccommodationAssessmentStatus.unallocated -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.inReview  -> CasReferralStatus.PENDING
      TemporaryAccommodationAssessmentStatus.readyToPlace  -> CasReferralStatus.ACCEPTED
      TemporaryAccommodationAssessmentStatus.closed -> CasReferralStatus.REJECTED
      TemporaryAccommodationAssessmentStatus.rejected  -> CasReferralStatus.REJECTED
    },
    date = casReferral.createdAt
  )