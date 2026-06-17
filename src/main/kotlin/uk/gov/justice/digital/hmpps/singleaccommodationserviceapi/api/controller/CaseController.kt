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

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list")
  fun getCases(
    @RequestParam riskLevel: RiskLevel?,
    @RequestParam searchTerm: String?,
    @RequestParam teamCode: String?,
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> {
    val personDtos = caseQueryService.getCaseList()
    val upstreamFailures = personDtos.upstreamFailures.toMutableList()

    val crnsToPrisonNumbers = personDtos.data.associate { it.crn to it.nomsNumber }
    val caseListCrns = personDtos.data.map { it.crn }
    val unpersistedCrns = caseApplicationService.findUnpersistedCrns(caseListCrns)
    if (unpersistedCrns.isNotEmpty()) {
      val casesToAdd = caseApplicationService.getCasesFromOrchestrator(unpersistedCrns)
      upstreamFailures += casesToAdd.upstreamFailures
      caseApplicationService.upsertCases(casesToAdd.data, crnsToPrisonNumbers)
    }
    val result =
      caseQueryService.getCases(personDtos.data, searchTerm = searchTerm, riskLevel = riskLevel, teamCode = teamCode)

    return ResponseEntity.ok(ApiResponseDto(data = result, upstreamFailures = upstreamFailures))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<ApiResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCase(crn))
}
