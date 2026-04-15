package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant

data class AuditRecordDto(
  val type: AuditRecordType,
  val author: String,
  val commitDate: Instant,
  val changes: List<FieldChange>,
)

enum class AuditRecordType {
  CREATE,
  UPDATE,
}

interface FieldChange {
  val field: String
  val value: String?
}

data class UpdateFieldChangeDto(
  override val field: String,
  override val value: String?,
  val oldValue: String?,
) : FieldChange

data class CreateFieldChangeDto(
  override val field: String,
  override val value: String?,
) : FieldChange
