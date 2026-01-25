package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ProposedAccommodationApplicationService

@RestController
class ProposedAccommodationController(
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getAll(@PathVariable crn: String): ResponseEntity<List<AccommodationDetail>> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.proposedAccommodations) }
    ?: ResponseEntity.notFound().build()

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
