package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dummy

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

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

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/sequential")
  fun sequential(@RequestBody crns: List<String>): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(
    dummyService.getInfoSequential(crns),
  )

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/coroutine/{crn}")
  suspend fun coroutine(@PathVariable crn: String): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(
    dummyService.getResult(crn),
  )

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/coroutine")
  suspend fun coroutine(@RequestBody crns: List<String>): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(
    dummyService.getResults(crns),
  )
}
