package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail

@RestController
class ProposedAccommodationController {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getPrivateAddressesByCrn(@PathVariable crn: String): ResponseEntity<List<AccommodationDetail>> {
    log.warn("/cases/{crn}/proposed-accommodations has not been implemented.")
    return ResponseEntity.noContent().build()
  }
}
