package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseService

@RestController
class CaseController(
  private val caseService: CaseService,
) {

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
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

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<CaseDto> {
    val case = caseService.getCase(crn)
    return ResponseEntity.ok(case)
  }
}

// TODO fill in enums for Status
enum class Status
