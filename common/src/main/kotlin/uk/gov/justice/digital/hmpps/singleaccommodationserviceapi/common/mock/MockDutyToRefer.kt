package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import java.util.UUID

fun getMockedDutyToRefers(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb0"),
      submittedTo = "Mock District Council A!!",
      reference = "mock-abcd!!",
      submitted = mockedLocalDateTime,
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  availableCrnList[1] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb1"),
      submittedTo = "Mock District Council B!!",
      reference = "mock-abcd!!",
      submitted = mockedLocalDateTime,
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  availableCrnList[2] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb2"),
      submittedTo = "Mock District Council C!!",
      reference = "mock-abcd!!",
      submitted = mockedLocalDateTime,
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  availableCrnList[3] -> listOf(
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb3"),
      submittedTo = "Mock District Council D!!",
      reference = "mock-abcd!!",
      submitted = mockedLocalDateTime,
      status = "submitted!!",
      outcome = "pending!!",
    ),
    DutyToReferDto(
      crn = crn,
      id = UUID.fromString("b5e22e29-36bf-48e5-bfc9-915176298cb3"),
      submittedTo = "Mock District Council F!!",
      reference = "mock-abcd!!",
      submitted = mockedLocalDateTime,
      status = "submitted!!",
      outcome = "pending!!",
    ),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
