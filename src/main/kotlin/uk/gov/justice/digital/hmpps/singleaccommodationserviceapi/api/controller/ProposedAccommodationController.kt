package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData

@RestController
class ProposedAccommodationController(
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getPrivateAddressesByCrn(@PathVariable crn: String): ResponseEntity<List<AccommodationDetail>> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.proposedAccommodations) }
    ?: ResponseEntity.notFound().build()
}
