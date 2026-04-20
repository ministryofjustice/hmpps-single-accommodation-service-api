package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService

@RestController
class AccommodationHistoryController(
  private val accommodationQueryService: AccommodationQueryService,
) {
  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}/accommodation-history")
  fun getAccommodationHistory(@PathVariable crn: String): ResponseEntity<ApiResponseDto<List<AccommodationSummaryDto>>> = ResponseEntity.ok(accommodationQueryService.getAccommodationHistory(crn))

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/v2/cases/{crn}/accommodation-history")
  fun getAccommodationHistoryV2(@PathVariable crn: String): ResponseEntity<ApiResponseDto<List<AccommodationSummaryDto>>> = ResponseEntity.ok(accommodationQueryService.getAccommodationHistoryV2(crn))
}
