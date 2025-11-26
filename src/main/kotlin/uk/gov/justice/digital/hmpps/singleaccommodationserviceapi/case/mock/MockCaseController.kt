package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.mock.MockCase

@RestController
class MockCaseController {
  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/mock/cases")
  fun getMockCases(): ResponseEntity<List<Case>> = ResponseEntity.ok(MockCase.getMockedCases())

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/mock/cases/{crn}")
  fun getMockCase(@PathVariable crn: String): ResponseEntity<List<Case>> = ResponseEntity.ok(
    MockCase.getMockedCases()
      .filter { it.crn == crn },
  )
}
