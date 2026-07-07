package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationDto

fun buildAccommodationReferralOrchestrationDto(
  cas1Referrals: List<Cas1ReferralHistory> = listOf(
    buildReferralHistory(
      Cas1ReferralHistory.ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      referredBy = buildDeliusUserDto(),
      placementAddress = "Some address",
      placementStatus = Cas1ReferralHistory.Cas1SpaceBookingStatus.ARRIVED,
    ),
  ),
  cas3Referrals: List<Cas3ReferralHistory> = listOf(
    buildReferralHistory(
      Cas3ReferralHistory.ApplicationStatus.SUBMITTED,
      Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus.READY_TO_PLACE,
      referralRejectionReason = "Some reason",
      localAuthorityArea = "Some area",
      pdu = "Some pdu",
      placementAddress = "Some address",
      bookingStatus = Cas3ReferralHistory.Cas3BookingStatus.CONFIRMED,
    ),
  ),
) = AccommodationReferralOrchestrationDto(
  cas1Referrals = cas1Referrals,
  cas3Referrals = cas3Referrals,
)
