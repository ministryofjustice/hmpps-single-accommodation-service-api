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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.SingleAccommodationServiceApiExceptionHandler.Companion.handleUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ProposedAccommodationApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTimelineService
import java.util.UUID

@RestController
class ProposedAccommodationController(
  private val caseQueryService: CaseQueryService,
  private val caseApplicationService: CaseApplicationService,
  private val accommodationQueryService: AccommodationQueryService,
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
  private val proposedAccommodationQueryService: ProposedAccommodationQueryService,
  private val proposedAccommodationTimelineService: ProposedAccommodationTimelineService,
  private val accommodationSyncService: AccommodationSyncService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/proposed-accommodations")
  fun getAll(@PathVariable crn: String): ResponseEntity<ApiResponseDto<List<ProposedAccommodationDto>>> {
    if (!caseQueryService.isPersistedCase(crn)) {
      val result = caseQueryService.getCaseFromDelius(crn)
      handleUpstreamFailure(result.upstreamFailures)
      caseApplicationService.upsertCase(crn, result.data!!.nomsNumber)
    }

    val cprAccommodations = accommodationQueryService.getAllAccommodations(crn)
    handleUpstreamFailure(cprAccommodations.upstreamFailures)
    accommodationSyncService.syncAccommodationFromDelius(
      crn,
      cprAccommodations.data,
    )
    return ResponseEntity.ok(ApiResponseDto(data = proposedAccommodationQueryService.getProposedAccommodations(crn)))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun getById(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<ProposedAccommodationDto>> {
    val accommodation = proposedAccommodationQueryService.getProposedAccommodation(crn, id)
    return ResponseEntity.ok(ApiResponseDto(data = accommodation))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/proposed-accommodations/{id}/timeline")
  fun getTimeline(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<List<AuditRecordDto>>> {
    val timelineEntries = proposedAccommodationTimelineService.getProposedAccommodationTimeline(id, crn)
    return ResponseEntity.ok(ApiResponseDto(data = timelineEntries))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/proposed-accommodations")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody request: ProposedAccommodationDetailCommand,
  ): ResponseEntity<ProposedAccommodationDto> {
    val currentAccommodation = accommodationQueryService.getCurrentAccommodation(crn)
    handleUpstreamFailure(currentAccommodation.upstreamFailures)
    val createdProposedAccommodation = proposedAccommodationApplicationService.createProposedAccommodation(crn, currentAccommodation.data, request)
    return ResponseEntity(createdProposedAccommodation, HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/proposed-accommodations/{id}/notes")
  @ResponseStatus(HttpStatus.CREATED)
  fun createNote(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: NoteCommand,
  ): ResponseEntity<Void> {
    val currentAccommodation = accommodationQueryService.getCurrentAccommodation(crn)
    handleUpstreamFailure(currentAccommodation.upstreamFailures)
    proposedAccommodationApplicationService.createProposedAccommodationNote(id, crn, request, currentAccommodation.data)
    return ResponseEntity(HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PutMapping("/cases/{crn}/proposed-accommodations/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: ProposedAccommodationDetailCommand,
  ): ResponseEntity<ProposedAccommodationDto> {
    val currentAccommodation = accommodationQueryService.getCurrentAccommodation(crn)
    handleUpstreamFailure(currentAccommodation.upstreamFailures)
    val updatedProposedAccommodation = proposedAccommodationApplicationService.updateProposedAccommodation(id, crn, request, currentAccommodation.data)
    return ResponseEntity.ok(updatedProposedAccommodation)
  }
}
