package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Cas3PremisesSearchResults
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.WebClientConfig
import java.util.UUID

@Component
class ApprovedPremisesApiClient(
  @Qualifier("approvedPremisesApiWebClient") private val webClientConfig: WebClientConfig,
) {

  private val premisesSearchPath = "/sas/premises/search"

  fun getPremises(postcode: String, probationRegionId: UUID): ClientResult<Cas3PremisesSearchResults> {
    return try {
      val responseBody = webClientConfig.webClient.get()
        .uri("$premisesSearchPath?postcode=$postcode&probationRegionId=$probationRegionId")
        .retrieve()
        .bodyToMono(Cas3PremisesSearchResults::class.java)
        .block()!!

      ClientResult.Success(HttpStatus.OK, responseBody)
    } catch (ex: WebClientResponseException) {
      ClientResult.Failure.StatusCode("GET", premisesSearchPath, ex.statusCode, ex.responseBodyAsString)
    } catch (ex: Exception) {
      ClientResult.Failure.Other("GET", premisesSearchPath, ex)
    }
  }
}
