package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant
import java.util.UUID

enum class CasReferralStatus {
  ACCEPTED,
  REJECTED,
  PENDING,
  ;
  companion object
}

data class AccommodationReferralDto(
  val id: UUID,
  val type: CasService,
  val status: CasReferralStatus,
  val date: Instant,
)

enum class CasService {
  CAS1,
  CAS2,
  CAS2v2,
  CAS3,
}
