package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dutytorefer.DutyToReferDto
import java.time.LocalDateTime
import java.util.UUID

fun getMockedDutyToRefers(crn: String) = listOf(
  DutyToReferDto(
    crn = crn,
    id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb0"),
    submittedTo = "Mock District Council!!",
    reference = "mock-abcd!!",
    submitted = LocalDateTime.parse("1970-01-01T00:00:00"),
    status = "submitted!!",
    outcome = "pending!!",
  ),
)
