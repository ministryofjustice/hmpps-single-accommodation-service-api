package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

data class AuditRecordDto(
  val type: AuditRecordType,
  val author: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val commitDate: Instant?,
  val changes: List<FieldChange>,
  @field:JsonInclude(JsonInclude.Include.NON_NULL)
  val extraInformation: Map<String, String>? = null,
)

enum class AuditRecordType {
  CREATE,
  UPDATE,
  NOTE,
}

interface FieldChange {
  var field: String
  var value: String?
}

data class UpdateFieldChangeDto(
  override var field: String,
  override var value: String?,
  var oldValue: String?,
) : FieldChange

data class CreateFieldChangeDto(
  override var field: String,
  override var value: String?,
) : FieldChange
