package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.WebClientConfig

data class CaseSummaries(
  val cases: List<Case>,
)

sealed class ClientResult<out T> {
  data class Success<T>(val status: HttpStatus, val body: T) : ClientResult<T>()
  sealed class Failure : ClientResult<Nothing>() {
    data class StatusCode(
      val method: String,
      val path: String,
      val status: HttpStatusCode,
      val body: String?,
    ) : Failure()

    data class Other(
      val method: String,
      val path: String,
      val throwable: Throwable,
    ) : Failure()
  }

  fun throwException(): Nothing = when (this) {
    is Success -> error("Cannot throw exception for success result")
    is Failure.StatusCode ->
      error("AP Delius API error $status on $method $path: $body")
    is Failure.Other ->
      throw throwable
  }
}

@Component
class ApDeliusContextApiClient(
  @Qualifier("apDeliusContextApiWebClient") private val webClientConfig: WebClientConfig,
) {

  private val caseSummariesPath = "/probation-cases/summaries"

  fun getCaseSummaries(crnsOrNomsNumbers: List<String>): ClientResult<CaseSummaries> {
    if (crnsOrNomsNumbers.isEmpty()) {
      return ClientResult.Success(HttpStatus.OK, CaseSummaries(emptyList()))
    }

    return try {
      val responseBody = webClientConfig.webClient.post()
        .uri(caseSummariesPath)
        .bodyValue(crnsOrNomsNumbers)
        .retrieve()
        .bodyToMono(CaseSummaries::class.java)
        .block()!!

      ClientResult.Success(HttpStatus.OK, responseBody)
    } catch (ex: WebClientResponseException) {
      ClientResult.Failure.StatusCode("POST", caseSummariesPath, ex.statusCode, ex.responseBodyAsString)
    } catch (ex: Exception) {
      ClientResult.Failure.Other("POST", caseSummariesPath, ex)
    }
  }
}
