package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ProposedAccommodationApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import java.util.UUID

@RestController
class ProposedAccommodationController(
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
  private val proposedAccommodationQueryService: ProposedAccommodationQueryService,
) {

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getAll(@PathVariable crn: String): ResponseEntity<List<AccommodationDetail>> {
    val accommodations = proposedAccommodationQueryService.getProposedAccommodations(crn)
    return ResponseEntity.ok(accommodations)
  }

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun getById(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<AccommodationDetail> {
    val accommodation = proposedAccommodationQueryService.getProposedAccommodation(crn, id)
    return ResponseEntity.ok(accommodation)
  }

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @PostMapping("/cases/{crn}/proposed-accommodations")
  fun create(
    @PathVariable crn: String,
    @RequestBody request: AccommodationDetailCommand,
  ): ResponseEntity<AccommodationDetail> {
    val createdProposedAccommodation = proposedAccommodationApplicationService.createProposedAccommodation(crn, request)
    return ResponseEntity(createdProposedAccommodation, HttpStatus.CREATED)
  }

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN')")
  @GetMapping("/proposed-accommodations/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<AccommodationDetail> {
    val accommodation = proposedAccommodationQueryService.getProposedAccommodation(id)
    return ResponseEntity.ok(accommodation)
  }

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @PutMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: AccommodationDetailCommand,
  ): ResponseEntity<AccommodationDetail> {
    val updatedProposedAccommodation = proposedAccommodationApplicationService.updateProposedAccommodation(crn, id, request)
    return ResponseEntity.ok(updatedProposedAccommodation)
  }
}
