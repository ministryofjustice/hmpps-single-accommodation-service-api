package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev")
class RedissonMasterSlaveServersConfig {

  @Bean(name = ["redissonClient"], destroyMethod = "shutdown")
  fun redissonClient(
    @Value($$"${redis.host}") redisHost: String,
    @Value($$"${redis.replica.host}") redisReplicaHost: String,
    @Value($$"${redis.port}") redisPort: Int,
    @Value($$"${redis.auth.token}") authToken: String,
    @Value($$"${redisson.timeout}") redissonTimeout: Int,
    @Value($$"${redisson.retry.attempts}") redissonRetryAttempts: Int,
    @Value($$"${redisson.retry.interval}") redissonRetryInterval: Int,
    @Value($$"${redisson.connnection.pool-size}") redissonConnectionPoolSize: Int,
    @Value($$"${redisson.connnection.minimum-idle-size}") redissonConnectionMinimumIdleSize: Int,
  ): RedissonClient {
    val masterAddress = "rediss://$redisHost:$redisPort"
    val replicaAddress = "rediss://$redisReplicaHost:$redisPort"
    val config = Config()
    config.useMasterSlaveServers()
      .setMasterAddress(masterAddress)
      .addSlaveAddress(replicaAddress)
      .setPassword(authToken)
      .setTimeout(redissonTimeout)
      .setRetryAttempts(redissonRetryAttempts)
      .setRetryInterval(redissonRetryInterval)
      .setMasterConnectionPoolSize(redissonConnectionPoolSize)
      .setMasterConnectionMinimumIdleSize(redissonConnectionMinimumIdleSize)
      .setSlaveConnectionPoolSize(redissonConnectionPoolSize)
      .setSlaveConnectionMinimumIdleSize(redissonConnectionMinimumIdleSize)
    return Redisson.create(config)
  }
}

@Configuration
@Profile("local")
class RedissonLocalConfig {

  @Bean(name = ["redissonClient"], destroyMethod = "shutdown")
  fun redissonLocalClient(
    @Value($$"${redis.host}") redisHost: String,
    @Value($$"${redis.port}") redisPort: Int,
    @Value($$"${redisson.timeout}") redissonTimeout: Int,
    @Value($$"${redisson.retry.interval}") redissonRetryInterval: Int,
    @Value($$"${redisson.retry.attempts}") redissonRetryAttempts: Int,
    @Value($$"${redisson.connnection.pool-size}") redissonConnectionPoolSize: Int,
    @Value($$"${redisson.connnection.minimum-idle-size}") redissonConnectionMinimumIdleSize: Int,
  ): RedissonClient {
    val config = Config()
    config
      .useSingleServer()
      .setAddress("redis://$redisHost:$redisPort")
      .setPassword(null)
      .setConnectionPoolSize(redissonConnectionPoolSize)
      .setConnectionMinimumIdleSize(redissonConnectionMinimumIdleSize)
      .setTimeout(redissonTimeout)
      .setRetryAttempts(redissonRetryAttempts)
      .setRetryInterval(redissonRetryInterval)

    return Redisson.create(config)
  }
}

@Configuration
@EnableCaching
class RedissonCacheConfig {

  @Bean
  fun cacheManager(redissonClient: RedissonClient): CacheManager {
    val configs = mapOf(
      "getRoshSummaryByCrn" to CacheConfig(60_000, 30_000),
      "getCaseSummaryByCrn" to CacheConfig(120_000, 60_000),
      "getCorePersonRecordByCrn" to CacheConfig(180_000, 120_000),
      "getTierByCrn" to CacheConfig(180_000, 120_000),
      "getAccommodationStatus" to CacheConfig(180_000, 120_000),

    )
    return RedissonSpringCacheManager(redissonClient, configs)
  }
}
