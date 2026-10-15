package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sas_user_custom_case_list")
class UserCustomCaseListEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  val sasUserId: UUID,
  val sasCaseId: UUID,
  val createdAt: Instant = Instant.now(),
)
