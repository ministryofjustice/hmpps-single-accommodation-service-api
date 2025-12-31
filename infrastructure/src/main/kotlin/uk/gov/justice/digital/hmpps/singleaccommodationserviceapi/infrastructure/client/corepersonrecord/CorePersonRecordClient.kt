package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface CorePersonRecordClient {
  @GetExchange(value = "/person/probation/{crn}")
  fun getCorePersonRecord(@PathVariable crn: String): CorePersonRecord
}

@Service
open class CorePersonRecordCachingService(
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD, key = "#crn", sync = true)
  open fun getCorePersonRecord(crn: String) = corePersonRecordClient.getCorePersonRecord(crn)
}
