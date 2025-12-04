package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas3ReferralHistory

data class AccommodationReferralOrchestrationDto(
  val cas1Referrals: List<Cas1ReferralHistory>,
  val cas2Referrals: List<Cas2ReferralHistory>,
  val cas2v2Referrals: List<Cas2v2ReferralHistory>,
  val cas3Referrals: List<Cas3ReferralHistory>,
)
