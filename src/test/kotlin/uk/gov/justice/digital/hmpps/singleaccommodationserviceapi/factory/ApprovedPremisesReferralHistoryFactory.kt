package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.TemporaryAccommodationAssessmentStatus
import java.time.Instant
import java.util.UUID

fun buildCas1ReferralHistory(
  id: UUID = UUID.randomUUID(),
  status: Cas1AssessmentStatus = Cas1AssessmentStatus.IN_PROGRESS,
  createdAt: Instant,
) = listOf(
  Cas1ReferralHistory(
    type = ServiceType.CAS1,
    id = id,
    applicationId = UUID.randomUUID(),
    status = status,
    createdAt = createdAt,
  ),
)

fun buildCas2ReferralHistory(
  id: UUID = UUID.randomUUID(),
  status: String = "Awaiting decision",
  createdAt: Instant,
) = listOf(
  Cas2ReferralHistory(
    type = ServiceType.CAS2,
    id = id,
    applicationId = UUID.randomUUID(),
    status = status,
    createdAt = createdAt,
  ),
)

fun buildCas2v2ReferralHistory(
  id: UUID = UUID.randomUUID(),
  status: String = "Place offered",
  createdAt: Instant,
) = listOf(
  Cas2v2ReferralHistory(
    type = ServiceType.CAS2v2,
    id = id,
    applicationId = UUID.randomUUID(),
    status = status,
    createdAt = createdAt,
  ),
)

fun buildCas3ReferralHistory(
  id: UUID = UUID.randomUUID(),
  status: TemporaryAccommodationAssessmentStatus = TemporaryAccommodationAssessmentStatus.IN_REVIEW,
  createdAt: Instant,
) = listOf(
  Cas3ReferralHistory(
    type = ServiceType.CAS3,
    id = id,
    applicationId = UUID.randomUUID(),
    status = status,
    createdAt = createdAt,
  ),
)
