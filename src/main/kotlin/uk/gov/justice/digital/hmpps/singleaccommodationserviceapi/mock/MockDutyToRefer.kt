package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dutytorefer.DutyToReferDto
import java.time.LocalDateTime
import java.util.UUID

fun getMockedDutyToRefers(crn: String) = when (crn) {
  mockCrns[0] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb0"),
      submittedTo = "Mock District Council A!!",
      reference = "mock-abcd!!",
      submitted = LocalDateTime.parse("1970-01-01T00:00:00"),
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  mockCrns[1] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb1"),
      submittedTo = "Mock District Council B!!",
      reference = "mock-abcd!!",
      submitted = LocalDateTime.parse("1970-01-01T00:00:00"),
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  mockCrns[2] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb2"),
      submittedTo = "Mock District Council C!!",
      reference = "mock-abcd!!",
      submitted = LocalDateTime.parse("1970-01-01T00:00:00"),
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  mockCrns[3] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb3"),
      submittedTo = "Mock District Council D!!",
      reference = "mock-abcd!!",
      submitted = LocalDateTime.parse("1970-01-01T00:00:00"),
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
