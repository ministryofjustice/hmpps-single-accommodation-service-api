package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaQueryService

enum class ReferenceDataType {
  LOCAL_AUTHORITY_AREAS,
}

@RestController
class ReferenceDataController(
  private val localAuthorityAreaQueryService: LocalAuthorityAreaQueryService,
) {

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/reference-data")
  fun getReferenceData(
    @RequestParam(required = true) type: ReferenceDataType,
  ): ResponseEntity<List<ReferenceDataDto>> = when (type) {
    ReferenceDataType.LOCAL_AUTHORITY_AREAS -> ResponseEntity.ok(localAuthorityAreaQueryService.getLocalAuthorityAreas())
  }
}
