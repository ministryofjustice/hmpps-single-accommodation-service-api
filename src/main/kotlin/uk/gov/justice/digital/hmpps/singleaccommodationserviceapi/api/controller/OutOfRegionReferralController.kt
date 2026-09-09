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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.OutOfRegionReferralApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.outofregionreferral.OutOfRegionReferralQueryService
import java.util.UUID

@RestController
class OutOfRegionReferralController(
  private val outOfRegionReferralApplicationService: OutOfRegionReferralApplicationService,
  private val outOfRegionReferralQueryService: OutOfRegionReferralQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/out-of-region-referral/{id}")
  fun getByCrnAndId(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<OutOfRegionReferralDto>> {
    val outOfRegionReferral = outOfRegionReferralQueryService.getOutOfRegionReferral(crn, id)
    return ResponseEntity.ok(ApiResponseDto(data = outOfRegionReferral))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/out-of-region-referral")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody command: OorCommand,
  ): ResponseEntity<OutOfRegionReferralDto> {
    val createdOutOfRegionReferral = outOfRegionReferralApplicationService.createOutOfRegionReferral(crn, command)
    return ResponseEntity(createdOutOfRegionReferral, HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/out-of-region-referral/{id}/timeline")
  fun getTimeline(
    @PathVariable crn: String,
    @PathVariable id: UUID,
  ): ResponseEntity<ApiResponseDto<List<AuditRecordDto>>> {
    val timelineEntries = outOfRegionReferralQueryService.getOutOfRegionReferralTimeline(id, crn)
    return ResponseEntity.ok(timelineEntries)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/out-of-region-referral/{id}/notes")
  @ResponseStatus(HttpStatus.CREATED)
  fun createNote(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: NoteCommand,
  ): ResponseEntity<Void> {
    outOfRegionReferralApplicationService.createOutOfRegionReferralNote(crn, id, request)
    return ResponseEntity(HttpStatus.CREATED)
  }

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN')")
  @GetMapping("/out-of-region-referrals/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponseDto<OutOfRegionReferralDto>> {
    val outOfRegionReferral = outOfRegionReferralQueryService.getOutOfRegionReferral(id)
    return ResponseEntity.ok(ApiResponseDto(data = outOfRegionReferral))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PutMapping("/cases/{crn}/out-of-region-referral/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody command: OorCommand,
  ): ResponseEntity<OutOfRegionReferralDto> {
    val updatedOutOfRegionReferral = outOfRegionReferralApplicationService.updateOutOfRegionReferral(crn, id, command)
    return ResponseEntity.ok(updatedOutOfRegionReferral)
  }
}
