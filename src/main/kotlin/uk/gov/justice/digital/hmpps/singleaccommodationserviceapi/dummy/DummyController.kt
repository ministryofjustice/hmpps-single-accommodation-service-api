package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dummy

import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case.DummyService

@RestController
class DummyController(private val dummyService: DummyService) {

  @GetMapping("/hello-world")
  fun helloWorld(): Mono<ResponseEntity<String>> = Mono.just(
    ResponseEntity.ok(
      """ 
      {"message": "hello world"}
      """.trimIndent(),
    ),
  )

  @PreAuthorize("permitAll()")
  @GetMapping("/sequential")
  fun sequential(): ResponseEntity<Map<String, String>> =
    ResponseEntity.ok(
      dummyService.getInfoSequential(),
    )

  @PreAuthorize("permitAll()")
  @GetMapping("/coroutine")
  fun coroutine(): ResponseEntity<Map<String, String>> = runBlocking {
    ResponseEntity.ok(
      dummyService.getInfoCoroutine(),
    )
  }

  @PreAuthorize("permitAll()")
  @GetMapping("/vt")
  fun virtualThreads(): ResponseEntity<Map<String, String>> =
    ResponseEntity.ok(
      dummyService.getInfoVirtualThreads(),
    )


}