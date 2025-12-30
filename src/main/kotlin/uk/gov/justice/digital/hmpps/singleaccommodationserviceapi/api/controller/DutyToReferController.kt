package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData

@RestController
class DutyToReferController(
  private val mockedData: MockData?,
) {
  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/dtrs/{crn}")
  fun getDutyToRefersByCrn(@PathVariable crn: String): ResponseEntity<List<DutyToReferDto>> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.dutyToRefers) }
    ?: ResponseEntity.notFound().build()
}
