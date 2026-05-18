package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices

import java.time.OffsetDateTime

data class CommissionedRehabilitativeServices(
  val status: CrsReferralStatus,
  val sentAt: OffsetDateTime?,
  val sentBy: CrsReferralSentBy?,
  val referral: CrsReferral?,
)

data class CrsReferralSentBy(
  val username: String,
  val authSource: String,
  val userId: String,
)

data class CrsReferral(
  val createdAt: OffsetDateTime?,
  val serviceProviders: List<CrsReferralServiceProvider>,
)

data class CrsReferralServiceProvider(
  val name: String,
  val id: String,
)

enum class CrsReferralStatus {
  DRAFT,
  LIVE,
  COMPLETED,
  WITHDRAWN,
}
