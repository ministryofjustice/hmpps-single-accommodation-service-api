package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DummyController {

  @GetMapping("/hello-world")
  fun helloWorld(): ResponseEntity<String> = ResponseEntity.ok(
    """ 
      {"message": "hello world"}
    """.trimIndent(),
  )
}
