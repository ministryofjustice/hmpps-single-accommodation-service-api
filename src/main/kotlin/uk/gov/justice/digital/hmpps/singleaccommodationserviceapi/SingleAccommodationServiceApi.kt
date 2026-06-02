package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SingleAccommodationServiceApi

fun main(args: Array<String>) {
  runApplication<SingleAccommodationServiceApi>(*args)
}
