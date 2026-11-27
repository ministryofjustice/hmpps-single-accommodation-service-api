package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Operation
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

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodation-history")
  @Operation(
    summary = "Provides a list of all past and current addresses, ordered by start date, newest first. As the start date of a current prison date is unknown, it will always be added at the start of the list",
    description = "If the CRN is currently in prison, their prison address will be returned, with the end date set to their release date\n\n" +
      "All other addresses are sourced from CPR, excluding any Proposed (PR) and Proposed for Resettlement (PR1), which should be the only addresses in CPR representing future stays. Note that this will include a current stay",
  )
  fun getAccommodationHistory(@PathVariable crn: String): ResponseEntity<ApiResponseDto<List<AccommodationSummaryDto>>> = ResponseEntity.ok(accommodationQueryService.getAccommodationHistory(crn))
}
