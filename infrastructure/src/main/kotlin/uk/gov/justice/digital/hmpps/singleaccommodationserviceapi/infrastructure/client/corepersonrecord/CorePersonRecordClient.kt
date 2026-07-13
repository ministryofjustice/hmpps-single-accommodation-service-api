package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddressResponse

interface CorePersonRecordClient {
  @GetExchange(value = "/person/probation/{crn}")
  fun getByCrn(@PathVariable crn: String): CorePersonRecord

  @GetExchange(value = "/person/prison/{prisonNumber}")
  fun getByPrisonNumber(@PathVariable prisonNumber: String): CorePersonRecord

  @PostExchange(value = "/person/probation/{crn}/address")
  fun createProbationAddress(@PathVariable crn: String, @RequestBody address: ProbationCreateAddress): ProbationCreateAddressResponse
}

@Service
class CorePersonRecordCachingService(
  private val corePersonRecordClient: CorePersonRecordClient,
  private val cacheManager: CacheManager,
) {
  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN, key = "#crn", sync = true)
  fun getCorePersonRecordByCrn(crn: String) = corePersonRecordClient.getByCrn(crn)

  @CacheEvict(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN, key = "#crn")
  fun createProbationAddress(crn: String, address: ProbationCreateAddress) = corePersonRecordClient.createProbationAddress(crn, address)

  @Cacheable(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_PRISON_NUMBER, key = "#prisonNumber", sync = true)
  fun getCorePersonRecordByNoms(prisonNumber: String) = corePersonRecordClient.getByPrisonNumber(prisonNumber)

  fun cacheEvictOnCorePersonRecordByCrn(crn: String) = cacheManager.getCache(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)?.evict(crn)
}
