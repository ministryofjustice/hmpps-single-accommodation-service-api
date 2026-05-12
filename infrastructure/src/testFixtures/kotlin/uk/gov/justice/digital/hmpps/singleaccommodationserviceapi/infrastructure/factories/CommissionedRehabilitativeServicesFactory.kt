package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import java.time.OffsetDateTime

fun buildCommissionedRehabilitativeServices(
  status: CrsReferralStatus = CrsReferralStatus.COMPLETED,
  sentAt: OffsetDateTime = OffsetDateTime.now(),
) = CommissionedRehabilitativeServices(
  status = status,
  sentAt = sentAt,
  sentBy = null,
  referral = null,
)
