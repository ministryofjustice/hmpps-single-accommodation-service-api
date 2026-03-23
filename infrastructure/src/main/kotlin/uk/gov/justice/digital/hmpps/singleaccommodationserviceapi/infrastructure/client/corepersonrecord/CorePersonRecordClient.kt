package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface CorePersonRecordClient {
  @GetExchange(value = "/person/probation/{crn}")
  fun getCorePersonRecord(@PathVariable crn: String): CorePersonRecord

  @PostExchange(value = "/person/probation/bulk")
  fun postCorePersonRecords(@RequestBody crns: List<String>): List<CorePersonRecord>
}

@Service
open class CorePersonRecordCachingService(
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD, key = "#crn", sync = true)
  open fun getCorePersonRecord(crn: String) = corePersonRecordClient.getCorePersonRecord(crn)

  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORDS, key = "#crns", sync = true)
  open fun getCorePersonRecords(crns: List<String>) = corePersonRecordClient.postCorePersonRecords(crns)
}
