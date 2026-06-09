package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant
import java.util.UUID

data class AccommodationReferralDto(
  val id: UUID,
  val type: AccommodationService,
  val status: AccommodationReferralStatus,
  val date: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: StaffDetailsDto?,
  val placementAddress: String?,
  val placementStatus: String?,
)

enum class AccommodationReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
  WITHDRAWN,
}

enum class AccommodationService {
  CAS1,
  CAS2,
  CAS2v2,
  CAS3,
  DTR,
}
