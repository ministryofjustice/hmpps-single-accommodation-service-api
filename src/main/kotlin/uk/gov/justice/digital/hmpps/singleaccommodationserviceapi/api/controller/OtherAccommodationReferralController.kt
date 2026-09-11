package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.OtherAccommodationReferralApplicationService
import java.util.UUID

@RestController
class OtherAccommodationReferralController(
  private val otherAccommodationReferralApplicationService: OtherAccommodationReferralApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/other-accommodation-referral")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody command: OtherAccommodationReferralCommand,
  ): ResponseEntity<OtherAccommodationReferralDto> {
    val created = otherAccommodationReferralApplicationService.createOtherAccommodationReferral(crn, command)
    return ResponseEntity(created, HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PutMapping("/cases/{crn}/other-accommodation-referral/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody command: OtherAccommodationReferralCommand,
  ): ResponseEntity<OtherAccommodationReferralDto> {
    val updatedOtherAccommodationReferral =
      otherAccommodationReferralApplicationService.updateOtherAccommodationReferral(crn, id, command)
    return ResponseEntity.ok(updatedOtherAccommodationReferral)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/other-accommodation-referral/{id}/notes")
  @ResponseStatus(HttpStatus.CREATED)
  fun createNote(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: NoteCommand,
  ): ResponseEntity<Void> {
    otherAccommodationReferralApplicationService.createOtherAccommodationReferralNote(crn, id, request)
    return ResponseEntity(HttpStatus.CREATED)
  }
}
