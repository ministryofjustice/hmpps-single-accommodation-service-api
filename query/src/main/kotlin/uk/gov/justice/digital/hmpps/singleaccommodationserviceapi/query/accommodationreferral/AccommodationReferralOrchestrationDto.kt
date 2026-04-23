package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory

data class AccommodationReferralOrchestrationDto(
  val cas1Referrals: List<Cas1ReferralHistory>,
  val cas2Referrals: List<Cas2ReferralHistory>,
  val cas2v2Referrals: List<Cas2ReferralHistory>,
  val cas3Referrals: List<Cas3ReferralHistory>,
)
