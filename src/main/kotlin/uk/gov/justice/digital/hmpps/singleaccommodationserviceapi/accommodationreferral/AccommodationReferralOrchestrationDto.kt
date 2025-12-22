package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.TemporaryAccommodationAssessmentStatus

data class AccommodationReferralOrchestrationDto(
  val cas1Referrals: List<ReferralHistory<Cas1AssessmentStatus>>,
  val cas2Referrals: List<ReferralHistory<Cas2Status>>,
  val cas2v2Referrals: List<ReferralHistory<Cas2Status>>,
  val cas3Referrals: List<ReferralHistory<TemporaryAccommodationAssessmentStatus>>,
)
