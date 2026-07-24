package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class CacheHelper(private val cacheManager: CacheManager) {
  fun cacheValueByCrn(
    crn: String,
    cacheKey: String,
    cacheValue: Any,
  ) {
    cacheManager.getCache(cacheKey)!!.put(crn, cacheValue)
    assertCacheEntryExists(crn, cacheKey)
  }

  fun assertCacheEntryExists(crn: String, cacheKey: String) {
    assertThat(cacheManager.getCache(cacheKey)!!.get(crn)).isNotNull
  }

  fun assertCacheEntryEvicted(crn: String, cacheKey: String) {
    assertThat(cacheManager.getCache(cacheKey)!!.get(crn)).isNull()
  }
}
