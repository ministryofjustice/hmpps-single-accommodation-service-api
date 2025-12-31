package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDateTime
import java.util.UUID

data class DutyToReferDto(
    val id: UUID,
    val crn: String,
    val submittedTo: String?,
    val reference: String?,
    val submitted: LocalDateTime?,
    val status: String?,
    val outcome: String?,
)