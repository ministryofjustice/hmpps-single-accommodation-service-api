package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationdatadomain.AccommodationDataDomainQueryService

@RestController
class AccommodationDataDomainController(private val accommodationDataDomainQueryService: AccommodationDataDomainQueryService) {
  @Operation(summary = "Calls the /health endpoint of the accommodation data domain api")
  @PreAuthorize(value = "hasAnyRole('ROLE_PROBATION', 'ROLE_ACCOMMODATION_DATA_DOMAIN__SINGLE_ACCOMMODATION_SERVICE')")
  @GetMapping("/accommodation-data-domain/health")
  fun getAccommodationDataDomainHealth(): ResponseEntity<String> = ResponseEntity.ok(accommodationDataDomainQueryService.getHealth())
}
