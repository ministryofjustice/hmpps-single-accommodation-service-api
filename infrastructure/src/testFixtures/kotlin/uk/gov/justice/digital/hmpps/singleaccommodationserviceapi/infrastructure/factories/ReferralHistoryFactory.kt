package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import java.time.Instant
import java.util.UUID

fun buildReferralHistory(
  casService: CasService,
  status: Cas1ReferralHistory.Cas1AssessmentStatus?,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas1ReferralHistory(
  casService = casService,
  id = id,
  applicationId = applicationId,
  status = status as Cas1ReferralHistory.Cas1AssessmentStatus,
  createdAt = createdAt,
)

fun buildReferralHistory(
  casService: CasService,
  status: Cas2ReferralHistory.Cas2Status?,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas2ReferralHistory(
  casService = casService,
  id = id,
  applicationId = applicationId,
  status = status as Cas2ReferralHistory.Cas2Status,
  createdAt = createdAt,
)

fun buildReferralHistory(
  casService: CasService,
  status: Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus?,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = Cas3ReferralHistory(
  casService = casService,
  id = id,
  applicationId = applicationId,
  status = status as Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus,
  createdAt = createdAt,
)
