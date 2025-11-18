package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor

import org.redisson.api.RedissonClient
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointCacheConfig
import java.util.concurrent.ConcurrentHashMap

@Component
class EndpointCacheManager(
  private val redissonClient: RedissonClient,
) {
  private val cacheManagers = ConcurrentHashMap<String, CacheManager>()
  private val caches = ConcurrentHashMap<String, Cache>()

  fun getCache(cacheConfig: EndpointCacheConfig): Cache = caches.computeIfAbsent(cacheConfig.cacheName) {
    val cacheManager = getOrCreateCacheManager(cacheConfig)
    cacheManager.getCache(cacheConfig.cacheName)
      ?: throw IllegalStateException("Failed to create cache '${cacheConfig.cacheName}'")
  }

  private fun getOrCreateCacheManager(cacheConfig: EndpointCacheConfig): CacheManager = cacheManagers.computeIfAbsent(cacheConfig.cacheName) {
    val ttlMillis = cacheConfig.ttl.toMillis()
    val maxIdleMillis = ttlMillis / 2 // Max idle is half of TTL

    val configs = mapOf(
      cacheConfig.cacheName to CacheConfig(ttlMillis, maxIdleMillis),
    )

    RedissonSpringCacheManager(redissonClient, configs)
  }
}
