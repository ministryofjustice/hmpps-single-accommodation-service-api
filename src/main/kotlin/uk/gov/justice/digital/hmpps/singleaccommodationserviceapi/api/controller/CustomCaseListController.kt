package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import jakarta.validation.ValidationException
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CustomCaseListApplicationService

private val CRN_REGEX = Regex("(?i)^[A-Z][0-9]{6}$")
private const val CRN_FORMAT_MESSAGE = "CRN must be in format A123456"

@Validated
@RestController
class CustomCaseListController(
  private val userService: UserService,
  private val caseApplicationService: CaseApplicationService,
  private val customCaseListApplicationService: CustomCaseListApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/case-list/custom")
  @ResponseStatus(HttpStatus.CREATED)
  fun createCustomCaseList(
    @RequestBody
    @Size(min = 1, max = 500, message = "Between 1 and 500 CRNs must be provided")
    crns: List<String>,
  ): ResponseEntity<Void> {
    val normalisedCrns = crns.map { it.trim().uppercase() }
    val invalidCrns = normalisedCrns.filterNot { CRN_REGEX.matches(it) }

    if (invalidCrns.isNotEmpty()) {
      throw ValidationException("$CRN_FORMAT_MESSAGE: ${invalidCrns.joinToString()}")
    }

    val user = userService.authorizeAndRetrieveUser()
    val distinctCrns = normalisedCrns.distinct()
    caseApplicationService.createCases(
      distinctCrns.map { CrnToPrisonNumber(it, null) },
      createAsBlankRecord = true,
      validateCrns = true,
    )
    customCaseListApplicationService.createCustomCaseList(user.id, distinctCrns)
    return ResponseEntity(HttpStatus.CREATED)
  }
}
