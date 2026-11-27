package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
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
  @Operation(
    summary = "Returns the current address for the CRN",
    description = "If the CRN is currently in prison, return the prison's address, using their release date as the end date.\n\n" +
      "Otherwise, source the MAIN address from CPR. If the address is a CAS1 or CAS3 type, update start and end dates using booking information retrieved from CAS1/3",
  )
  fun getCurrentAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> = ResponseEntity.ok(accommodationQueryService.getCurrentAccommodation(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/next")
  @Operation(
    summary = "Returns the next address for the CRN",
    description = "If there is an UPCOMING CAS1 address (i.e. not arrived), return it.\n\n" +
      "Otherwise, if there is a CONFIRMED CAS3 address (i.e. not arrived), return it. \n\n" +
      "Otherwise, return any Proposed (PR) or Proposed for Resettlement Address (PR1) from CPR where a postcode is defined and there is no end date. If there are multiple such addresses, one is picked indeterminately\n\n" +
      "This approach is required because PR/PR1 addresses do not have dates. From prison a CRN would goto a CAS1 or a CAS3. From CAS1 they would goto CAS3 or other accommodation type (i.e. PR/PR1 in CPR).",
  )
  fun getNextAccommodation(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummaryDto?>> = ResponseEntity.ok(accommodationQueryService.getNextAccommodation(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/accommodations/summary")
  @Operation(
    summary = "Provides status, current and next accommodation",
    description = "See documentation on current and next accommodation for further information",
  )
  fun getAccommodations(@PathVariable crn: String): ResponseEntity<ApiResponseDto<AccommodationSummariesDto>> = ResponseEntity.ok(accommodationQueryService.getAccommodationSummariesResponse(crn))

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD')")
  @GetMapping("/accommodations/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponseDto<AccommodationDetailDto>> {
    val accommodation = accommodationQueryService.getAccommodation(id)
    return ResponseEntity.ok(ApiResponseDto(data = accommodation))
  }
}
