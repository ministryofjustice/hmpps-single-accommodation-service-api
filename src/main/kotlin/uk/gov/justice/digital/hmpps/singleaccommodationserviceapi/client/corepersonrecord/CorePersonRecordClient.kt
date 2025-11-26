package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface CorePersonRecordClient {

  @GetExchange(value = "/person/probation/{crn}")
  fun getCorePersonRecord(@PathVariable crn: String): CorePersonRecord
}
