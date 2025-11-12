package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class Runner(
  private val webfluxService: WebfluxService
) {

  @PostConstruct
  public fun run() {
    webfluxService.doSomething()
  }

}