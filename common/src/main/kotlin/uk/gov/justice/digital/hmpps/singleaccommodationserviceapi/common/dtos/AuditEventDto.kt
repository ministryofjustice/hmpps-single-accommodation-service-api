package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant

data class AuditEventDto(
  val type: AuditEventType,
  val commitId: String,
  val commitDate: Instant,
  val changes: List<FieldChangeDto>,
)

enum class AuditEventType {
  CREATE,
  UPDATE,
}

data class FieldChangeDto(
  val field: String,
  val oldValue: Any?,
  val newValue: Any?,
)
