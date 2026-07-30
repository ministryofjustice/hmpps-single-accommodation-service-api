package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry

import io.sentry.SamplingContext
import io.sentry.SentryOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.util.pattern.PathPatternParser

@Component
class TracesSamplerCallback(
  @Value("\${sentry.traces-sample-rate:0.01}")
  private val tracesSampleRate: Double,
) : SentryOptions.TracesSamplerCallback {

  private val ignoredPathPatterns = listOf("/health/**").let { patterns ->
    val parser = PathPatternParser()
    patterns.map { pattern -> parser.parse(pattern) }
  }

  @Suppress("MagicNumber")
  override fun sample(context: SamplingContext): Double? {
    val parentSampled = context.transactionContext.parentSampled
    if (parentSampled != null) {
      return if (parentSampled) tracesSampleRate else 0.0
    }

    val path = PathContainer.parsePath(removeHttpMethodPrefix(context.transactionContext.name))
    if (ignoredPathPatterns.any { it.matches(path) }) {
      return 0.0
    }

    return null
  }

  private fun removeHttpMethodPrefix(transactionName: String) = transactionName
    .removePrefix("GET ")
    .removePrefix("PUT ")
    .removePrefix("POST ")
    .removePrefix("PATCH ")
    .removePrefix("DELETE ")
}
