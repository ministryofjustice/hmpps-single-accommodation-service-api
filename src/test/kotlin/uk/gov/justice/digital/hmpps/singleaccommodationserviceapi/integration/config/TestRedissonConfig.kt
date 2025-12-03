package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import redis.embedded.RedisServer

@TestConfiguration
class TestRedissonConfig {

  private lateinit var redisServer: RedisServer
  private lateinit var redissonClientInstance: RedissonClient

  @Value($$"${spring.data.redis.host}")
  lateinit var redisHost: String

  @Value($$"${spring.data.redis.port}")
  lateinit var redisPort: Integer

  @PostConstruct
  fun startRedis() {
    redisServer = RedisServer(redisPort.toInt())
    redisServer.start()
    println("Embedded Redis started on $redisHost:$redisPort")
  }

  @PreDestroy
  fun stopRedis() {
    if (::redissonClientInstance.isInitialized) {
      redissonClientInstance.shutdown()
      println("RedissonClient shutdown")
    }
    if (::redisServer.isInitialized) {
      redisServer.stop()
      println("Embedded Redis stopped")
    }
  }

  @Bean
  fun redissonClient(): RedissonClient {
    val config = Config().apply {
      useSingleServer().address = "redis://$redisHost:$redisPort"
    }
    redissonClientInstance = Redisson.create(config)
    return redissonClientInstance
  }
}
