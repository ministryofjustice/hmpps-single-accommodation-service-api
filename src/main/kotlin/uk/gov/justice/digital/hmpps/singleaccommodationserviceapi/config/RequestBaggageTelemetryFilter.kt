package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.stereotype.Component

@Component
class RequestBaggageTelemetryFilter : Filter {
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    Baggage.current().getEntryValue(USER_LOCATION_CODE)
      ?.takeIf { it.isNotBlank() }
      ?.let { Span.current().setAttribute(USER_LOCATION_CODE, it) }

    chain.doFilter(request, response)
  }

  companion object {
    private const val USER_LOCATION_CODE = "userLocationCode"
  }
}
