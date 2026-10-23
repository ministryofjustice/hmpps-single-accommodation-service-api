package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CustomCaseListApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListQueryService

private val CRN_REGEX = Regex("(?i)^[A-Z][0-9]{6}$")
private const val CRN_FORMAT_MESSAGE = "CRN must be in format A123456"

@Validated
@RestController
class CustomCaseListController(
  private val userService: UserService,
  private val caseApplicationService: CaseApplicationService,
  private val customCaseListApplicationService: CustomCaseListApplicationService,
  private val customCaseListQueryService: CustomCaseListQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list/custom")
  fun getCustomCaseList(): ResponseEntity<ApiResponseDto<List<CaseDto>>> = ResponseEntity.ok(customCaseListQueryService.getCustomCaseList())

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list/custom/crns")
  fun getCustomCaseListCrns(): ResponseEntity<ApiResponseDto<List<String>>> = ResponseEntity.ok(customCaseListQueryService.getCustomCaseListCrns())

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/case-list/custom")
  @ResponseStatus(HttpStatus.CREATED)
  fun createCustomCaseList(
    @RequestBody
    @Size(min = 1, max = 500, message = "Between 1 and 500 CRNs must be provided")
    crns: List<String>,
  ): ResponseEntity<ApiResponseDto<Pair<String, List<String>>>> {
    val normalisedCrns = crns.map { it.trim().uppercase() }
    val invalidCrns = normalisedCrns.filterNot { CRN_REGEX.matches(it) }
    if (invalidCrns.isNotEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponseDto("InvalidCrns:" to invalidCrns))
    }

    // bring the check for valid crns up to this level.
    val notFoundCrns = caseApplicationService.findInvalidOrCreate(normalisedCrns)
    if (notFoundCrns.isNotEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponseDto("NotFoundCrns:" to notFoundCrns))
    }

    val user = userService.authorizeAndRetrieveUser()

    customCaseListApplicationService.createCustomCaseList(user.id, normalisedCrns.distinct())
    return ResponseEntity(HttpStatus.CREATED)
  }
}
