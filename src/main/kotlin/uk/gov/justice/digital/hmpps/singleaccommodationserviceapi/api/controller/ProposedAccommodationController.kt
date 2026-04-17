package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ProposedAccommodationApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import java.util.UUID

@RestController
class ProposedAccommodationController(
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
  private val proposedAccommodationQueryService: ProposedAccommodationQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getAll(@PathVariable crn: String): ResponseEntity<ApiResponseDto<List<AccommodationDetail>>> {
    val accommodations = proposedAccommodationQueryService.getProposedAccommodations(crn)
    return ResponseEntity.ok(ApiResponseDto(data = accommodations))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun getById(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<AccommodationDetail>> {
    val accommodation = proposedAccommodationQueryService.getProposedAccommodation(crn, id)
    return ResponseEntity.ok(ApiResponseDto(data = accommodation))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}/proposed-accommodations/{id}/timeline")
  fun getTimeline(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<List<AuditRecordDto>>> {
    val timelineEntries = proposedAccommodationQueryService.getProposedAccommodationTimeline(id, crn)
    return ResponseEntity.ok(ApiResponseDto(data = timelineEntries))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @PostMapping("/cases/{crn}/proposed-accommodations")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody request: AccommodationDetailCommand,
  ): ResponseEntity<ApiResponseDto<AccommodationDetail>> {
    val createdProposedAccommodation = proposedAccommodationApplicationService.createProposedAccommodation(crn, request)
    return ResponseEntity(ApiResponseDto(data = createdProposedAccommodation), HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @PostMapping("/cases/{crn}/proposed-accommodations/{id}/notes")
  @ResponseStatus(HttpStatus.CREATED)
  fun createNote(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: NoteCommand,
  ): ResponseEntity<ApiResponseDto<Unit>> {
    proposedAccommodationApplicationService.createProposedAccommodationNote(crn, id, request)
    return ResponseEntity(ApiResponseDto(data = Unit), HttpStatus.CREATED)
  }

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN')")
  @GetMapping("/proposed-accommodations/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponseDto<AccommodationDetail>> {
    val accommodation = proposedAccommodationQueryService.getProposedAccommodation(id)
    return ResponseEntity.ok(ApiResponseDto(data = accommodation))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @PutMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: AccommodationDetailCommand,
  ): ResponseEntity<ApiResponseDto<AccommodationDetail>> {
    val updatedProposedAccommodation = proposedAccommodationApplicationService.updateProposedAccommodation(crn, id, request)
    return ResponseEntity.ok(ApiResponseDto(data = updatedProposedAccommodation))
  }
}
