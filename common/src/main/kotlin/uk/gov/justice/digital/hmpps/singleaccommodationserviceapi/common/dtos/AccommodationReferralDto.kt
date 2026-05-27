package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant
import java.util.UUID

data class AccommodationReferralDto(
  val id: UUID,
  val type: CasService,
  val status: CasReferralStatus,
  val date: Instant,
  val referralRejectionReason: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: AssignedToDto?,
  val placementAddress: String?,
  val placementStatus: String?,
)

enum class CasReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
}

enum class CasService {
  CAS1,
  CAS2,
  CAS2v2,
  CAS3,
}
