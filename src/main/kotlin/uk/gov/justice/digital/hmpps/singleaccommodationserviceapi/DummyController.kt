package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DummyController {

  @GetMapping("/hello-world", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun helloWorld(): ResponseEntity<String> = ResponseEntity.ok(
    """ 
      {"message": "hello world"}
    """.trimIndent(),
  )
}
