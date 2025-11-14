package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.model.Phone

@Service
class PhoneClient(
    @Qualifier("phoneApiWebClient") private val webClient: WebClient,
) {
  @Cacheable("phoneCache", key = "#id")
  fun getPhoneById(id: Long): Phone? {
    println("getPhoneById() Cache MISS — calling api...")
    return webClient
      .method(HttpMethod.GET)
      .uri("/objects")
      .retrieve()
      .toEntityList(Phone::class.java)
      .block()!!
      .body
      ?.firstOrNull { it.id == id }
  }
}