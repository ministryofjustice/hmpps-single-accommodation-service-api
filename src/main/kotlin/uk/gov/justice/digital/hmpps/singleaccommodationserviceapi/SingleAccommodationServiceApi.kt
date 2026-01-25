package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SingleAccommodationServiceApi

fun main(args: Array<String>) {
  runApplication<SingleAccommodationServiceApi>(*args)
}
