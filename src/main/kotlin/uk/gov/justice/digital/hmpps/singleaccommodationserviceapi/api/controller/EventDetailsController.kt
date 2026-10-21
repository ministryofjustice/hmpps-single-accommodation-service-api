package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrEventDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import java.util.UUID

@RestController
class EventDetailsController(
  private val dutyToReferQueryService: DutyToReferQueryService,
) {
  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__PROBATION_INTEGRATION')")
  @GetMapping("/event-details/duty-to-refers/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<DtrEventDetailsDto> {
    val eventDetails = dutyToReferQueryService.getEventDetailsDto(id)
    return ResponseEntity.ok(eventDetails)
  }
}
