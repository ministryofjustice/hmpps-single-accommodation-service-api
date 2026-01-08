package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseService
import kotlin.collections.map

@RestController
class CaseController(
  private val caseService: CaseService,
  private val mockedData: MockData?,
  private val crnList: List<String>,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) crns: List<String> = emptyList(),
  ): ResponseEntity<List<CaseDto>> {
    val cases = if (crns.isNotEmpty()) {
      caseService.getCases(crns, riskLevel)
    } else {
      caseService.getCases(crnList, riskLevel)
    }
    return mockedData
      ?.let {
        ResponseEntity.ok(
          cases.map {
            val currentMock = mockedData.crns[it.crn]!!
            it.copy(
              photoUrl = currentMock.photoUrl,
              nextAccommodation = currentMock.nextAccommodation,
              currentAccommodation = currentMock.currentAccommodation,
            )
          },
        )
      }
      ?: ResponseEntity.ok(cases)
  }

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<CaseDto> {
    val case = caseService.getCase(crn)
    return mockedData
      ?.let {
        val currentMock = mockedData.crns[case.crn]!!
        ResponseEntity.ok(
          case.copy(
            photoUrl = currentMock.photoUrl,
            nextAccommodation = currentMock.nextAccommodation,
            currentAccommodation = currentMock.currentAccommodation,
          ),
        )
      }
      ?: ResponseEntity.ok(case)
  }
}

// TODO fill in enums for Status
enum class Status
