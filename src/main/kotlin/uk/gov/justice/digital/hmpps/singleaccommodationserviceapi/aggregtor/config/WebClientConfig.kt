package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

  @Bean(name = ["phoneApiWebClient"])
  fun phoneApiWebClient(): WebClient {
    return WebClient.builder()
      .baseUrl("https://api.restful-api.dev")
      .build()
  }
}