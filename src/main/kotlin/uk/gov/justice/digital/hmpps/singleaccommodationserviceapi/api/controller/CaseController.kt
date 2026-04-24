package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService

@RestController
class CaseController(
  private val caseQueryService: CaseQueryService,
  private val caseApplicationService: CaseApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/case-list")
  fun getCases(@RequestParam riskLevel: RiskLevel?): ResponseEntity<ApiResponseDto<List<CaseDto>>> {
    val personDtos = caseQueryService.getCaseList()
    val crnsOnCaselist = personDtos.map { it.crn }
    val unpersistedCrns = caseApplicationService.findUnpersistedCrns(crnsOnCaselist)
    if (unpersistedCrns.isNotEmpty()) {
      val casesToAdd = caseApplicationService.getCasesFromOrchestrator(unpersistedCrns)
      caseApplicationService.upsertCases(casesToAdd)
    }
    val result = caseQueryService.getCases(personDtos).filter { riskLevel == null || it.riskLevel == riskLevel }
    return ResponseEntity.ok(ApiResponseDto(data = result))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<ApiResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCase(crn))
}
