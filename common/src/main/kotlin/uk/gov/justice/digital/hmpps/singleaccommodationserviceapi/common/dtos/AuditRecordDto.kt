package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class AuditRecordDto(
  val type: AuditRecordType,
  val author: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val commitDate: Instant,
  val changes: List<FieldChange>,
)

enum class AuditRecordType {
  CREATE,
  UPDATE,
  NOTE,
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
