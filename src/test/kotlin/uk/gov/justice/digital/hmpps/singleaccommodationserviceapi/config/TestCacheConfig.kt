package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestCacheConfig {

  @Bean
  @Primary
  fun cacheManager(): CacheManager = ConcurrentMapCacheManager()
}
