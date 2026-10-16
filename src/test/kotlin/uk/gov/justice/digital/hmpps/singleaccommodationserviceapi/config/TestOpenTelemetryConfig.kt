package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Wraps every request in a real (SDK-backed) span that is current on the request thread, and records finished spans
 * in memory, so tests can assert on the attributes set by the application while handling the request.
 */
@TestConfiguration
class TestOpenTelemetryConfig {
  @Bean
  fun testSpanExporter(): InMemorySpanExporter = InMemorySpanExporter.create()

  @Bean
  fun testSpanFilter(spanExporter: InMemorySpanExporter): FilterRegistrationBean<OncePerRequestFilter> {
    val tracer = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build()
      .get("test")

    val filter = object : OncePerRequestFilter() {
      override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
      ) {
        val span = tracer.spanBuilder("test-request").setSpanKind(SpanKind.SERVER).startSpan()
        try {
          span.makeCurrent().use { filterChain.doFilter(request, response) }
        } finally {
          span.end()
        }
      }
    }

    return FilterRegistrationBean<OncePerRequestFilter>(filter).apply { order = Ordered.HIGHEST_PRECEDENCE }
  }
}
