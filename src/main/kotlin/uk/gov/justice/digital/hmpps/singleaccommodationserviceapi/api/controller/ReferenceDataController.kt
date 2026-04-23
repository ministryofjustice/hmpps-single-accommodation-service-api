package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.AccommodationTypeQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaQueryService

enum class ReferenceDataType {
  LOCAL_AUTHORITY_AREAS,
  ACCOMMODATION_TYPES,
}

@RestController
class ReferenceDataController(
  private val localAuthorityAreaQueryService: LocalAuthorityAreaQueryService,
  private val accommodationTypeQueryService: AccommodationTypeQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/reference-data")
  fun getReferenceData(
    @RequestParam(required = true) type: ReferenceDataType,
  ): ResponseEntity<ApiResponseDto<List<ReferenceDataDto>>> = when (type) {
    ReferenceDataType.LOCAL_AUTHORITY_AREAS -> ResponseEntity.ok(localAuthorityAreaQueryService.getLocalAuthorityAreas())
    ReferenceDataType.ACCOMMODATION_TYPES -> ResponseEntity.ok(accommodationTypeQueryService.getAccommodationTypes())
  }
}
