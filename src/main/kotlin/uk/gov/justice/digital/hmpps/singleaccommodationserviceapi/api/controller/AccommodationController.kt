package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto

@RestController
class AccommodationController {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/accommodations")
  fun getAccommodations(@PathVariable crn: String): ResponseEntity<AccommodationDto> {
    log.warn("/cases/{crn}/accommodations has not been implemented.")
    return ResponseEntity.ok(null)
  }
}
