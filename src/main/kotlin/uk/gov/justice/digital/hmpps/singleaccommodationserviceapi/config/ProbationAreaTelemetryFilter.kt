package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.stereotype.Component

@Component
class ProbationAreaTelemetryFilter : Filter {
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    Baggage.current().getEntryValue(BAGGAGE_KEY)
      ?.takeIf { it.isNotBlank() }
      ?.let { Span.current().setAttribute(BAGGAGE_KEY, it) }

    chain.doFilter(request, response)
  }

  companion object {
    private const val BAGGAGE_KEY = "probationArea"
  }
}
