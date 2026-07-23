package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import java.time.Instant
import kotlin.String

fun buildAuditRecordDto(
  type: AuditRecordType = AuditRecordType.CREATE,
  author: String = "Delius User",
  commitDate: Instant = Instant.now(),
  changes: List<FieldChange> = listOf(buildFieldChange()),
  authorDetails: AssignedToDto? = null,
) = AuditRecordDto(
  type = type,
  author = author,
  authorDetails = authorDetails,
  commitDate = commitDate,
  changes = changes,
)

fun buildFieldChange(
  field: String = "nextAccommodationStatus",
  value: String? = "YES",
) = FieldChange(
  field = field,
  value = value,
)
