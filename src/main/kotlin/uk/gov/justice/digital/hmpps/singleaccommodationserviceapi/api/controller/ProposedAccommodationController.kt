package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ProposedAccommodationApplicationService

@RestController
class ProposedAccommodationController(
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getAll(@PathVariable crn: String): ResponseEntity<List<AccommodationDetail>> {
    log.warn("/cases/{crn}/proposed-accommodations has not been implemented.")
    return ResponseEntity.noContent().build()
  }

  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  @PostMapping("/cases/{crn}/proposed-accommodations")
  fun create(
    @PathVariable crn: String,
    @RequestBody request: CreateAccommodationDetail,
  ): ResponseEntity<AccommodationDetail> {
    val response = proposedAccommodationApplicationService.createProposedAccommodation(crn, request)
    return ResponseEntity.ok(response)
  }
}
