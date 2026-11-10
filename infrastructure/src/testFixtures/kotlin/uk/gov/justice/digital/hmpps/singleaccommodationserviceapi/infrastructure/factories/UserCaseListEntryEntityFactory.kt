package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCaseListEntryEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.util.UUID

@TestData
fun buildUserCaseListEntryEntity(
  id: UUID = UUID.randomUUID(),
  sasUserId: UUID = UUID.randomUUID(),
  sasCaseId: UUID = UUID.randomUUID(),
  listType: CaseListType = CaseListType.CUSTOM,
  createdAt: Instant = Instant.now(),
  lastUpdatedAt: Instant = createdAt,
) = UserCaseListEntryEntity(
  id = id,
  sasUserId = sasUserId,
  sasCaseId = sasCaseId,
  listType = listType,
  createdAt = createdAt,
  lastUpdatedAt = lastUpdatedAt,
)
