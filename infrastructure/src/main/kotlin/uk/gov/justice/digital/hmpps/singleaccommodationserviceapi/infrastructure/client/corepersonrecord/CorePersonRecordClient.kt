package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface CorePersonRecordClient {
  @GetExchange(value = "/person/probation/{crn}")
  fun getByCrn(@PathVariable crn: String): CorePersonRecord

  @GetExchange(value = "/person/prison/{prisonNumber}")
  fun getByPrisonNumber(@PathVariable prisonNumber: String): CorePersonRecord
}

@Service
class CorePersonRecordCachingService(
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN, key = "#crn", sync = true)
  fun getCorePersonRecordByCrn(crn: String) = corePersonRecordClient.getByCrn(crn)

  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_NOMS, key = "#prisonNumber", sync = true)
  fun getCorePersonRecordByNoms(prisonNumber: String) = corePersonRecordClient.getByPrisonNumber(prisonNumber)
}
