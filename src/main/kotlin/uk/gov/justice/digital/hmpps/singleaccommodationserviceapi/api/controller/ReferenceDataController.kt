package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityAreaDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.ReferenceDataQueryService

@RestController
class ReferenceDataController(
  private val referenceDataQueryService: ReferenceDataQueryService,
) {

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/reference-data/local-authority-areas")
  fun getLocalAuthorityAreas(
    @RequestParam(required = false) search: String?,
    @RequestParam(required = false, defaultValue = "true") active: Boolean,
  ): ResponseEntity<List<LocalAuthorityAreaDto>> = ResponseEntity.ok(referenceDataQueryService.getLocalAuthorityAreas(search, active))
}
