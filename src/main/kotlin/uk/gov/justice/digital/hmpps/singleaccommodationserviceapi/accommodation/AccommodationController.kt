package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockAccommodationResponse

@RestController
class AccommodationController {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("accommodation/{crn}")
  fun getAccommodation(@PathVariable crn: String): ResponseEntity<AccommodationResponse> = ResponseEntity.ok(mockAccommodationResponse(crn))
}
