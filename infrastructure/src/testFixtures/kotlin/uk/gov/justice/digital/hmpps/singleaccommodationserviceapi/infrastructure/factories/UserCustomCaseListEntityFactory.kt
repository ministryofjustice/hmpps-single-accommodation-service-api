package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.util.UUID

@TestData
fun buildUserCustomCaseListEntity(
  id: UUID = UUID.randomUUID(),
  sasUserId: UUID = UUID.randomUUID(),
  sasCaseId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
) = UserCustomCaseListEntity(
  id = id,
  sasUserId = sasUserId,
  sasCaseId = sasCaseId,
  createdAt = createdAt,
)
