package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData

@RestController
class AccommodationController(
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/accommodations")
  fun getAccommodations(@PathVariable crn: String): ResponseEntity<AccommodationDto> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.accommodation) }
    ?: ResponseEntity.notFound().build()
}
