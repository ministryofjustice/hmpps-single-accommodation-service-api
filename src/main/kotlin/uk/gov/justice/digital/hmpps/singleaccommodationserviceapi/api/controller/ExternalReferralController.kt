package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ExternalReferralApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral.ExternalReferralQueryService
import java.util.UUID

@RestController
class ExternalReferralController(
  private val externalReferralApplicationService: ExternalReferralApplicationService,
  private val externalReferralQueryService: ExternalReferralQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/external-referral/{id}")
  fun getByCrnAndId(@PathVariable crn: String, @PathVariable id: UUID): ResponseEntity<ApiResponseDto<ExternalReferralDto>> {
    val referral = externalReferralQueryService.getExternalReferral(crn, id)
    return ResponseEntity.ok(ApiResponseDto(data = referral))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/external-referral/search")
  fun searchExternalReferrals(
    @PathVariable crn: String,
    @Parameter(description = "Filter results to only referrals with one of these statuses.")
    @RequestParam(required = false) statuses: List<ExternalReferralStatus>?,
  ): ResponseEntity<ApiResponseDto<List<ExternalReferralDto>>> {
    val referrals = externalReferralQueryService.searchExternalReferrals(crn, statuses)
    return ResponseEntity.ok(ApiResponseDto(referrals))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/external-referral")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody command: ExternalReferralCommand,
  ): ResponseEntity<ExternalReferralDto> {
    val created = externalReferralApplicationService.createExternalReferral(crn, command)
    return ResponseEntity(created, HttpStatus.CREATED)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}/external-referral/{id}/timeline")
  fun getTimeline(
    @PathVariable crn: String,
    @PathVariable id: UUID,
  ): ResponseEntity<ApiResponseDto<List<AuditRecordDto>>> {
    val timelineEntries = externalReferralQueryService.getExternalReferralTimeline(id, crn)
    return ResponseEntity.ok(timelineEntries)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PutMapping("/cases/{crn}/external-referral/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody command: ExternalReferralCommand,
  ): ResponseEntity<ExternalReferralDto> {
    val updatedExternalReferral =
      externalReferralApplicationService.updateExternalReferral(crn, id, command)
    return ResponseEntity.ok(updatedExternalReferral)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/cases/{crn}/external-referral/{id}/notes")
  @ResponseStatus(HttpStatus.CREATED)
  fun createNote(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody request: NoteCommand,
  ): ResponseEntity<Void> {
    externalReferralApplicationService.createExternalReferralNote(crn, id, request)
    return ResponseEntity(HttpStatus.CREATED)
  }
}
