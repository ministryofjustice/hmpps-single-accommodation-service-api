package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestRedissonConfig {

  @Bean(destroyMethod = "shutdown")
  fun redissonClient(): RedissonClient {
    val config = Config()
    config.useSingleServer()
      .setAddress("redis://localhost:6379")
    return Redisson.create(config)
  }
}
