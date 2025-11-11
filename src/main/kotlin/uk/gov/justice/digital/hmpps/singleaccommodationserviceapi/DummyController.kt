package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model.Cas1PremisesBasicSummary

@RestController
class DummyController(
  private val dummyService: DummyService,
) {

  @GetMapping("/hello-world", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun helloWorld(): ResponseEntity<String> = ResponseEntity.ok(
    """ 
      {"message": "hello world"}
    """.trimIndent(),
  )

  @GetMapping("/premises/summary")
  fun getPremisesSummary(): ResponseEntity<List<Cas1PremisesBasicSummary>> = ResponseEntity.ok(
    dummyService.getPremisesSummary(),
  )
}
