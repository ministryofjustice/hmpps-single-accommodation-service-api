package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationDto
import java.time.Instant
import java.util.UUID

fun buildAccommodationReferralOrchestrationDto(
  cas1Referrals: List<ReferralHistory<Cas1AssessmentStatus>> = listOf(
    buildReferralHistory(CasService.CAS1, Cas1AssessmentStatus.COMPLETED),
  ),
  cas2Referrals: List<ReferralHistory<Cas2Status>> = listOf(
    buildReferralHistory(CasService.CAS2, Cas2Status.PLACE_OFFERED),
  ),
  cas2v2Referrals: List<ReferralHistory<Cas2Status>> = listOf(
    buildReferralHistory(CasService.CAS2v2, Cas2Status.AWAITING_DECISION),
  ),
  cas3Referrals: List<ReferralHistory<TemporaryAccommodationAssessmentStatus>> = listOf(
    buildReferralHistory(CasService.CAS3, TemporaryAccommodationAssessmentStatus.READY_TO_PLACE),
  ),
) = AccommodationReferralOrchestrationDto(
  cas1Referrals = cas1Referrals,
  cas2Referrals = cas2Referrals,
  cas2v2Referrals = cas2v2Referrals,
  cas3Referrals = cas3Referrals,
)

fun <T : CasStatus> buildReferralHistory(
  casService: CasService,
  status: T,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = ReferralHistory(
  casService = casService,
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
)
