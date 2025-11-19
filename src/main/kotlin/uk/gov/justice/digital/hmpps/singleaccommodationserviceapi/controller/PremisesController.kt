package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Cas3PremisesSearchResults
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.PremisesService
import java.util.UUID

@RestController
class PremisesController(
  private val premisesService: PremisesService,
) {

  @GetMapping("/premises")
  fun premises(
    @RequestParam postcode: String,
    @RequestParam probationRegionId: UUID
  ): ResponseEntity<Cas3PremisesSearchResults> {
    val premisesSearchResult = premisesService.searchPremises(postcode, probationRegionId)
    return ResponseEntity.ok(premisesSearchResult)
  }
}
