package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_ACCOMMODATION_RESPONSE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_SUMMARY
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_ROSH_DETAIL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_SUITABLE_CAS1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER

@Configuration
@Profile("dev", "preprod", "prod")
open class RedissonMasterSlaveServersConfig {

  @Bean(name = ["redissonClient"], destroyMethod = "shutdown")
  open fun redissonClient(
    @Value($$"${redis.host}") redisHost: String,
    @Value($$"${redis.replica.host}") redisReplicaHost: String,
    @Value($$"${redis.port}") redisPort: Int,
    @Value($$"${redis.auth.token}") authToken: String,
    @Value($$"${redisson.timeout}") redissonTimeout: Int,
    @Value($$"${redisson.retry.attempts}") redissonRetryAttempts: Int,
    @Value($$"${redisson.retry.interval}") redissonRetryInterval: Int,
    @Value($$"${redisson.connection.pool-size}") redissonConnectionPoolSize: Int,
    @Value($$"${redisson.connection.minimum-idle-size}") redissonConnectionMinimumIdleSize: Int,
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
open class RedissonLocalConfig {

  @Bean(name = ["redissonClient"], destroyMethod = "shutdown")
  open fun redissonLocalClient(
    @Value($$"${redis.host}") redisHost: String,
    @Value($$"${redis.port}") redisPort: Int,
    @Value($$"${redisson.timeout}") redissonTimeout: Int,
    @Value($$"${redisson.retry.interval}") redissonRetryInterval: Int,
    @Value($$"${redisson.retry.attempts}") redissonRetryAttempts: Int,
    @Value($$"${redisson.connection.pool-size}") redissonConnectionPoolSize: Int,
    @Value($$"${redisson.connection.minimum-idle-size}") redissonConnectionMinimumIdleSize: Int,
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
@Profile(value = ["local", "dev", "preprod", "prod"])
class RedissonCacheConfig {

  @Bean
  fun cacheManager(redissonClient: RedissonClient): CacheManager {
    val configs = mapOf(
      GET_ROSH_DETAIL to CacheConfig(60_000, 30_000),
      GET_CASE_SUMMARY to CacheConfig(120_000, 60_000),
      GET_CORE_PERSON_RECORD to CacheConfig(180_000, 120_000),
      GET_TIER to CacheConfig(180_000, 120_000),
      GET_ACCOMMODATION_RESPONSE to CacheConfig(180_000, 120_000),
      GET_PRISONER to CacheConfig(180_000, 120_000),
      GET_CAS1_REFERRAL to CacheConfig(60_000, 60_000),
      GET_CAS2_REFERRAL to CacheConfig(60_000, 60_000),
      GET_CAS2V2_REFERRAL to CacheConfig(60_000, 60_000),
      GET_CAS3_REFERRAL to CacheConfig(60_000, 60_000),
      GET_SUITABLE_CAS1_APPLICATION to CacheConfig(180_000, 120_000),
    )
    return RedissonSpringCacheManager(redissonClient, configs)
  }
}
