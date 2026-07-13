package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto

@RestController
class CaseController(
  private val caseQueryService: CaseQueryService,
  private val caseApplicationService: CaseApplicationService,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list")
  fun getCases(
    @RequestParam riskLevel: RiskLevel?,
    @RequestParam searchTerm: String?,
    @RequestParam teamCode: String?,
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> {
    val personDtos = caseQueryService.getCaseList()
    val upstreamFailures = personDtos.upstreamFailures.toMutableList()

    val filteredCaseList = caseQueryService.applyCaseListFilters(personDtos.data, searchTerm, riskLevel, teamCode)
    saveCases(filteredCaseList)
    // TODO: Add caseApplicationService.upsertCases() in here after MVP
    val caseDtos = caseQueryService.getCases(filteredCaseList)
    return ResponseEntity.ok(ApiResponseDto(data = caseDtos, upstreamFailures = upstreamFailures))
  }

  fun saveCases(filteredCaseList: List<PersonDto>) {
    filteredCaseList.chunked(25) { chunk ->
      val unpersistedCrns = caseQueryService.findUnpersistedCrns(filteredCaseList.map { it.crn })
      if (unpersistedCrns.isNotEmpty()) {
        val crnsToPrisonNumbers =
          chunk.filter { it.crn in unpersistedCrns }.map { CrnToPrisonNumber(it.crn, it.nomsNumber) }
        try {
          caseApplicationService.createCases(crnsToPrisonNumbers)
        } catch (d: DataIntegrityViolationException) {
          log.warn("Caught DataIngratityViolation When creating cases. Refiltering:\n{}", d.message)
          saveCases(chunk)
        }
      }
    }
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<ApiResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCase(crn))
}
