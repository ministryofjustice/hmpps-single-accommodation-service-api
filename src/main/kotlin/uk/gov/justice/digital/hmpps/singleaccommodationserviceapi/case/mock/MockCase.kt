package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.Case
import java.time.LocalDate

class MockCase {
  companion object {
    fun getMockedCases(): List<Case> {
      val cases: MutableList<Case> = mutableListOf()
      for (count in 1..10) {
        cases += Case(
          name = "Mock case $count",
          dateOfBirth = LocalDate.now().minusYears(count.toLong()),
          crn = "CRN000$count",
          prisonNumber = "PRN000$count",
          tier = "TODO()",
          rosh = "TODO()",
          pncReference = "TODO()",
          assignedTo = null,
          currentAccommodation = null,
          nextAccommodation = null,
        )
      }
      return cases
    }
  }
}
