package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddressLookupResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.addresslookup.AddressLookupService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.util.PostcodeValidator

@RestController
class AddressLookupController(
  private val addressLookupService: AddressLookupService,
) {
  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/address-lookup/postcode")
  fun byPostcode(
    @RequestParam postcode: String,
    @RequestParam(required = false, defaultValue = "50") maxResults: Int,
    @RequestParam(required = false, defaultValue = "0") offset: Int,
  ): ResponseEntity<AddressLookupResponse> {
    val normalised = PostcodeValidator.normalise(postcode)
    require(PostcodeValidator.isValid(normalised)) { "Invalid postcode: $postcode" }
    require(maxResults in 1..100) { "maxResults must be between 1 and 100" }
    require(offset >= 0) { "offset must be >= 0" }

    val result = addressLookupService.byPostcode(normalised, maxResults, offset)
    return ResponseEntity.ok(result)
  }
}
