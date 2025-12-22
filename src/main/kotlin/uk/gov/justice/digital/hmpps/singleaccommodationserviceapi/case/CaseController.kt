package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockCrns
import kotlin.collections.get
import kotlin.collections.map

@RestController
class CaseController(
  val caseService: CaseService,
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    // these crns are currently mocked
    @RequestParam(required = false) crns: List<String> = mockCrns,
  ): ResponseEntity<List<CaseDto>> {
    val cases = caseService.getCases(crns, riskLevel)
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
