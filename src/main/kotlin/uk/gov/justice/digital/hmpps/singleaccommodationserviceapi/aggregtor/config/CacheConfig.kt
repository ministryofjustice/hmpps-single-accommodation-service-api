package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.config

import org.redisson.api.RedissonClient
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {

  @Bean
  fun cacheManager(redissonClient: RedissonClient): CacheManager {
    val configs = mapOf(
      "phoneCache" to org.redisson.spring.cache.CacheConfig(60_000, 30_000),
    )
    return RedissonSpringCacheManager(redissonClient, configs)
  }
}
