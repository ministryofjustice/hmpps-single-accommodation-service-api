package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface CorePersonRecordClient {
  @GetExchange(value = "/person/probation/{crn}")
  fun getCorePersonRecord(@PathVariable crn: String): CorePersonRecord
}

@Service
class CorePersonRecordCachingService(
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  @Cacheable("getCorePersonRecordByCrn", key = "#crn")
  fun getCorePersonRecord(crn: String) = corePersonRecordClient.getCorePersonRecord(crn)
}
