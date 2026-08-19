package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SingleAccommodationServiceApi

fun main(args: Array<String>) {
  runApplication<SingleAccommodationServiceApi>(*args)
}
