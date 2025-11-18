package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import java.time.Duration

@Configuration
class AggregatorConfig(
  private val endpointRegistry: EndpointRegistry,
  private val aggregatorRegistry: AggregatorRegistry,
) {

  @Bean
  fun exampleEndpoints(): EndpointRegistry {
    endpointRegistry.endpoint("objects") {
      baseUrl = "https://api.restful-api.dev"
      path = "/objects"
      method = HttpMethod.GET
      cache {
        ttl = Duration.ofMinutes(10)
        cacheName = "objectsCache"
        keyStrategy = defaultCacheKeyStrategy("objects")
      }
    }

    endpointRegistry.endpoint("user") {
      baseUrl = "https://jsonplaceholder.typicode.com"
      path = "/users/{userId}"
      method = HttpMethod.GET
      cache {
        ttl = Duration.ofMinutes(15)
        cacheName = "userCache"
        keyStrategy = { params -> "user:${params["userId"]}" }
      }
      resilience {
        timeout = Duration.ofSeconds(5)
        circuitBreaker {
          failureRateThreshold = 50.0f
          waitDurationInOpenState = Duration.ofSeconds(10)
        }
      }
    }

    endpointRegistry.endpoint("posts") {
      baseUrl = "https://jsonplaceholder.typicode.com"
      path = "/posts"
      method = HttpMethod.GET
      cache {
        ttl = Duration.ofMinutes(5)
        cacheName = "postsCache"
      }
      resilience {
        timeout = Duration.ofSeconds(3)
        circuitBreaker {
          failureRateThreshold = 50.0f
          waitDurationInOpenState = Duration.ofSeconds(15)
          slidingWindowSize = 20
          minimumNumberOfCalls = 10
        }
        retry {
          maxAttempts = 3
          waitDuration = Duration.ofMillis(500)
        }
      }
    }

    // Example 4: Get post by ID (with path variable)
    endpointRegistry.endpoint("postById") {
      baseUrl = "https://jsonplaceholder.typicode.com"
      path = "/posts/{postId}"
      method = HttpMethod.GET
      cache {
        ttl = Duration.ofMinutes(10)
        cacheName = "postCache"
        keyStrategy = { params -> "post:${params["postId"]}" }
      }
      resilience {
        timeout = Duration.ofSeconds(5)
      }
    }

    // Example 5: Get comments with query parameters
    endpointRegistry.endpoint("comments") {
      baseUrl = "https://jsonplaceholder.typicode.com"
      path = "/comments"
      method = HttpMethod.GET
      cache {
        ttl = Duration.ofMinutes(2) // Short TTL for comments
        cacheName = "commentsCache"
        keyStrategy = { params ->
          val postId = params["postId"]?.toString() ?: "all"
          "comments:$postId"
        }
      }
      resilience {
        timeout = Duration.ofSeconds(5)
      }
    }

    return endpointRegistry
  }

  @Bean
  fun exampleAggregators(): AggregatorRegistry {
    // Define an aggregator that groups multiple endpoints for dashboard data
    aggregatorRegistry.aggregator("dashboardData") {
      endpoint("objects")
      endpoint("posts")
      endpoint("user")
    }

    // Aggregator for JSONPlaceholder endpoints
    aggregatorRegistry.aggregator("jsonPlaceholder") {
      endpoints("user", "posts", "postById", "comments")
    }

    // Simple aggregator with just two endpoints
    aggregatorRegistry.aggregator("simpleAggregation") {
      endpoints("objects", "posts")
    }

    return aggregatorRegistry
  }
}
