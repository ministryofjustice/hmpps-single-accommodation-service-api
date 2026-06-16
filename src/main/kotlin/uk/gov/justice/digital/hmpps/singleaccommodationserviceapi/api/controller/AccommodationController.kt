package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import java.util.UUID

@RestController
class AccommodationController(
  private val accommodationQueryService: AccommodationQueryService,
) {
  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/current")
  fun getCurrentAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> = ResponseEntity.ok(accommodationQueryService.getCurrentAccommodation(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/next")
  fun getNextAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> = ResponseEntity.ok(accommodationQueryService.getNextAccommodation(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD')")
  @GetMapping("/accommodations/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponseDto<AccommodationDetailDto>> {
    val accommodation = accommodationQueryService.getAccommodation(id)
    return ResponseEntity.ok(ApiResponseDto(data = accommodation))
  }
}
