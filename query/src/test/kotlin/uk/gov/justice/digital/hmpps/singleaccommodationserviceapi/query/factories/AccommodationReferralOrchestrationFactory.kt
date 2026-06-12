package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationDto

fun buildAccommodationReferralOrchestrationDto(
  cas1Referrals: List<Cas1ReferralHistory> = listOf(
    buildReferralHistory(
      Cas1ReferralHistory.Cas1AssessmentStatus.COMPLETED,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      referredBy = buildDeliusUserDto(),
      placementAddress = "Some address",
      placementStatus = "Some status",
    ),
  ),
  cas2HdcReferrals: List<Cas2ReferralHistory> = listOf(
    buildReferralHistory(
      Cas2ReferralHistory.Cas2Status.PLACE_OFFERED,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      referredBy = buildDeliusUserDto(),
      placementAddress = "Some address",
      placementStatus = "Some status",
    ),
  ),
  cas2v2Referrals: List<Cas2ReferralHistory> = listOf(
    buildReferralHistory(
      Cas2ReferralHistory.Cas2Status.AWAITING_DECISION,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      referredBy = buildDeliusUserDto(),
      placementAddress = "Some address",
      placementStatus = "Some status",
    ),
  ),
  cas3Referrals: List<Cas3ReferralHistory> = listOf(
    buildReferralHistory(
      Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus.READY_TO_PLACE,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      placementAddress = "Some address",
      placementStatus = "Some status",
    ),
  ),
) = AccommodationReferralOrchestrationDto(
  cas1Referrals = cas1Referrals,
  cas2HdcReferrals = cas2HdcReferrals,
  cas2Referrals = cas2v2Referrals,
  cas3Referrals = cas3Referrals,
)
