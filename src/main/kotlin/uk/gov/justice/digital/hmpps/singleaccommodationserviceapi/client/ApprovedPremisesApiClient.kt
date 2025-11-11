package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.WebClientConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model.Cas1PremisesBasicSummary

@Component
class ApprovedPremisesApiClient(
  @Qualifier("approvedPremisesApiWebClient") internal val webClientConfig: WebClientConfig,
  internal val objectMapper: ObjectMapper,
) {

  fun getPremises(): List<Cas1PremisesBasicSummary> {
    val result =
      webClientConfig
        .webClient
        .method(HttpMethod.GET)
        .uri("/premises/summary")
        .headers {
          it.addAll(
            HttpHeaders().apply {
              set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            },
          )
        }
        .retrieve()
        .toEntity(String::class.java)
        .retryWhen(Retry.max(webClientConfig.maxRetryAttempts))
        .block()!!

    return objectMapper.readValue(
      result.body,
      object : TypeReference<List<Cas1PremisesBasicSummary>>() {},
    )
  }
}
