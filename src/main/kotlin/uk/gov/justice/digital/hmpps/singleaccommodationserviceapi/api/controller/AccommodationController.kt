package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService

@RestController
class AccommodationController(
  private val accommodationQueryService: AccommodationQueryService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/current")
  fun getCurrentAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> = ResponseEntity.ok(accommodationQueryService.getCurrentAccommodation(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/next")
  fun getNextAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> {
    log.warn("/cases/{crn}/accommodations/next has not been implemented.")
    return ResponseEntity.ok(
      ApiResponseDto(
        data = null,
        upstreamFailures = emptyList(),
      ),
    )
  }
}
