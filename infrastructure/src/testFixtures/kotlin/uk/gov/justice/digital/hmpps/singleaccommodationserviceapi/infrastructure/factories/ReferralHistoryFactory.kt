package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.Instant
import java.util.UUID

fun buildReferralHistory(
  status: Cas1ReferralHistory.Cas1AssessmentStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto,
  placementAddress: String? = null,
  placementStatus: String? = null,
) = Cas1ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  placementStatus = placementStatus,
)

fun buildReferralHistory(
  status: Cas2ReferralHistory.Cas2Status,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto,
  placementAddress: String? = null,
  placementStatus: String? = null,
) = Cas2ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  placementStatus = placementStatus,
)

fun buildReferralHistory(
  status: Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto = buildDeliusUserDto(),
  placementAddress: String? = null,
  placementStatus: String? = null,
) = Cas3ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  placementStatus = placementStatus,
)

fun buildDeliusUserDto(name: String = "Joe Bloggs", username: String = "user1", staffCode: String = "ABCD1234") = DeliusUserDto(
  name = name,
  username = username,
  staffCode = staffCode,
)
