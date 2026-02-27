package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto

@RestController
class DutyToReferController {
  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/cases/{crn}/dtrs")
  fun getDutyToRefersByCrn(@PathVariable crn: String): ResponseEntity<List<DutyToReferDto>> {
    log.warn("/cases/{crn}/dtrs has not been implemented.")
    return ResponseEntity.ok(emptyList())
  }
}
