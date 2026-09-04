package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientResponseException

inline fun <T> getOrNullWhenNotFound(block: () -> T): T? = runCatching(block).getOrElse { throwable ->
  if (throwable is RestClientResponseException && throwable.statusCode == HttpStatus.NOT_FOUND) {
    null
  } else {
    throw throwable
  }
}
