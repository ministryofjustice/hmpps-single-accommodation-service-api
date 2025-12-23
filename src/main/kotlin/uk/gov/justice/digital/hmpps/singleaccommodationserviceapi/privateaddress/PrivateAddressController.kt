package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.MockData

@RestController
class PrivateAddressController(
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/private-addresses/{crn}")
  fun getPrivateAddressesByCrn(@PathVariable crn: String): ResponseEntity<PrivateAddressesDto> = mockedData
    ?.let { ResponseEntity.ok(it.crns[crn]!!.privateAddresses) }
    ?: ResponseEntity.notFound().build()
}
