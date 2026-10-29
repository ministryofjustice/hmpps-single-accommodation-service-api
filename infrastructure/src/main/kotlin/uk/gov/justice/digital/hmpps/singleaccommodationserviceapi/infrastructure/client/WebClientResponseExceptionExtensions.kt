package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

import org.springframework.web.reactive.function.client.WebClientResponseException

inline fun <T> getOrNullWhenNotFound(block: () -> T): T? = runCatching(block).getOrElse { throwable ->
  if (throwable is WebClientResponseException.NotFound) {
    null
  } else {
    throw throwable
  }
}
