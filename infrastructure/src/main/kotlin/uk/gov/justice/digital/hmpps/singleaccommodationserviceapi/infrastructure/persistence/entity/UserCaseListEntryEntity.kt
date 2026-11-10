package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sas_user_case_list_entry")
class UserCaseListEntryEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  val sasUserId: UUID,
  val sasCaseId: UUID,
  @Enumerated(EnumType.STRING)
  val listType: CaseListType,
  val createdAt: Instant = Instant.now(),
)

enum class CaseListType {
  CUSTOM,
  SEARCH_HISTORY,
}
