package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.CasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.CasesService

@Service
@RestController
@RequestMapping("/cases")
class CasesController(
  private val casesService: CasesService,
) {

  @GetMapping
  fun userCasesGet(): ResponseEntity<CasesResponse> {
    val cases = casesService.getUserCases()
    return ResponseEntity.ok(CasesResponse(cases))
  }
}
