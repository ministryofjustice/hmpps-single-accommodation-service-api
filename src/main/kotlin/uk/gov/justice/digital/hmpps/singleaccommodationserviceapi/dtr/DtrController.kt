package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dtr

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.MockDataDto

@RestController
class DtrController(
  @Qualifier("mockedData")
  private val mockedData: MockDataDto?,
) {
  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/dtrs/{crn}")
  fun getDtrsByCrn(@PathVariable crn: String): ResponseEntity<List<DtrDto>> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.dtrs) }
    ?: ResponseEntity.notFound().build()
}
