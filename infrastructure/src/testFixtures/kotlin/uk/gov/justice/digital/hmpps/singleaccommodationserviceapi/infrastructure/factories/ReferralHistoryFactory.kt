package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.Instant
import java.time.LocalDate
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
  referredBy: DeliusUserDto? = null,
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
  referredBy: DeliusUserDto? = null,
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
  referredBy: DeliusUserDto? = null,
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

fun buildDutyToReferDto(
  crn: String,
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: DtrStatus = DtrStatus.SUBMITTED,
  withdrawalReason: WithdrawalReason? = null,
  withdrawalReasonOther: String? = null,
  outcomeReason: OutcomeReason? = null,
  createdAt: Instant = Instant.now(),
) = DutyToReferDto(
  caseId,
  crn,
  status,
  submission = DtrSubmissionDto(
    id = id,
    localAuthority = LocalAuthorityDto(localAuthorityAreaId, "Test Local Authority"),
    referenceNumber = referenceNumber,
    submissionDate = submissionDate,
    createdBy = "Test User",
    createdAt = createdAt,
    withdrawalReason = withdrawalReason,
    withdrawalReasonOther = withdrawalReasonOther,
    outcomeReason = outcomeReason,
  ),
)
