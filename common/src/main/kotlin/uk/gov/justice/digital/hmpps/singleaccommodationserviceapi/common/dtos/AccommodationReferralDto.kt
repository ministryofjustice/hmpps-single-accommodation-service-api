package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant
import java.util.UUID

data class AccommodationReferralDto(
  val id: UUID,
  val type: AccommodationService,
  val status: AccommodationReferralStatus,
  val assessmentStatus: String?,
  val requestForPlacementStatus: String?,
  val date: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: StaffDetailsDto?,
  val placementAddress: String?,
  val placementStatus: String?,
  val uiUrl: String?,
)

enum class AccommodationReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
  WITHDRAWN,
  EXPIRED,
  NOT_ARRIVED,
  DEPARTED,
  CANCELLED,
  REQUEST_REJECTED,
  REQUEST_WITHDRAWN,
  ARCHIVED,
}

enum class AccommodationService {
  CAS1,
  CAS3,
  DTR,
}
