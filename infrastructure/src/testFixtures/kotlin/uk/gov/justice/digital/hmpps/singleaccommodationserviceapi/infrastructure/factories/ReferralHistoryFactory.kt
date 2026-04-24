package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import java.time.Instant
import java.util.UUID

fun buildReferralHistory(
  status: Cas1ReferralHistory.Cas1AssessmentStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas1ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
)

fun buildReferralHistory(
  status: Cas2ReferralHistory.Cas2Status,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas2ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
)

fun buildReferralHistory(
  status: Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas3ReferralHistory(
  id = id,
  applicationId = applicationId,
  status = status,
  createdAt = createdAt,
)
