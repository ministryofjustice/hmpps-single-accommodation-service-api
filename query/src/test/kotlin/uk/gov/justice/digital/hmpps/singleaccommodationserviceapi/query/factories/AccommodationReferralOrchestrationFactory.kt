package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationDto

fun buildAccommodationReferralOrchestrationDto(
  cas1Referrals: List<Cas1ReferralHistory> = listOf(
    buildReferralHistory(CasService.CAS1, Cas1ReferralHistory.Cas1AssessmentStatus.COMPLETED),
  ),
  cas2Referrals: List<Cas2ReferralHistory> = listOf(
    buildReferralHistory(CasService.CAS2, Cas2ReferralHistory.Cas2Status.PLACE_OFFERED),
  ),
  cas2v2Referrals: List<Cas2ReferralHistory> = listOf(
    buildReferralHistory(CasService.CAS2v2, Cas2ReferralHistory.Cas2Status.AWAITING_DECISION),
  ),
  cas3Referrals: List<Cas3ReferralHistory> = listOf(
    buildReferralHistory(
      CasService.CAS3,
      Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus.READY_TO_PLACE,
    ),
  ),
) = AccommodationReferralOrchestrationDto(
  cas1Referrals = cas1Referrals,
  cas2Referrals = cas2Referrals,
  cas2v2Referrals = cas2v2Referrals,
  cas3Referrals = cas3Referrals,
)
