package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CasStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ReferralHistory
import java.time.Instant
import java.util.UUID

fun buildReferralHistory(
  id: UUID = UUID.randomUUID(),
  casService: CasService,
  status: CasStatus,
  createdAt: Instant,
) = listOf(
  ReferralHistory(
    casService = casService,
    id = id,
    applicationId = UUID.randomUUID(),
    status = status,
    createdAt = createdAt,
  ),
)
