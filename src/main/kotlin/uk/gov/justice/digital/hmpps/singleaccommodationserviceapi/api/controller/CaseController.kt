package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseService

@RestController
class CaseController(
  private val caseService: CaseService,
  private val caseApplicationService: CaseApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) crns: List<String> = emptyList(),
  ): ResponseEntity<List<CaseDto>> {
    // TODO this allows for testing with multiple CRNs and will be removed in future.
    return if (crns.isNotEmpty()) {
      ResponseEntity.ok(caseService.getCases(crns, riskLevel))
    } else {
      ResponseEntity.ok(emptyList())
    }
  }

  // probably should make a separate v2/cases endpoint instead of editing this one
  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/case-list")
  fun getCases(
    @RequestParam(required = true) userName: String,
  ): ResponseEntity<List<CaseDto>> {
    // mutation
    val caseList = caseApplicationService.upsertCases(userName)

    // query
    return ResponseEntity.ok(caseService.getCaseList(caseList))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<CaseDto> {
    val case = caseService.getCase(crn)
    return ResponseEntity.ok(case)
  }
}

// TODO fill in enums for Status
enum class Status
