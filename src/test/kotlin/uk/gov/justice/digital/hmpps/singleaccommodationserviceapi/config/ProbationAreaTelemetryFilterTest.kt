package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import io.opentelemetry.api.baggage.Baggage
import io.opentelemetry.api.trace.Span
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ProbationAreaTelemetryFilterTest {
  private val filter = ProbationAreaTelemetryFilter()
  private val request = mockk<ServletRequest>(relaxed = true)
  private val response = mockk<ServletResponse>(relaxed = true)
  private val chain = mockk<FilterChain>(relaxed = true)

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `sets the probation area span attribute when baggage is present`() {
    val baggage = mockk<Baggage>()
    val span = mockk<Span>(relaxed = true)

    mockkStatic(Baggage::class)
    mockkStatic(Span::class)
    every { Baggage.current() } returns baggage
    every { baggage.getEntryValue(PROBATION_AREA) } returns "London"
    every { Span.current() } returns span

    filter.doFilter(request, response, chain)

    verifyOrder {
      Baggage.current()
      Span.current()
      span.setAttribute(PROBATION_AREA, "London")
      chain.doFilter(request, response)
    }
    confirmVerified(span, chain)
  }

  @Test
  fun `does not set the span attribute when baggage is missing`() {
    val baggage = mockk<Baggage>()
    val span = mockk<Span>(relaxed = true)

    mockkStatic(Baggage::class)
    mockkStatic(Span::class)
    every { Baggage.current() } returns baggage
    every { baggage.getEntryValue(PROBATION_AREA) } returns null
    every { Span.current() } returns span

    filter.doFilter(request, response, chain)

    verifyOrder {
      Baggage.current()
      chain.doFilter(request, response)
    }
    verify { span wasNot Called }
    confirmVerified(span, chain)
  }

  @Test
  fun `does not set the span attribute when baggage is blank`() {
    val baggage = mockk<Baggage>()
    val span = mockk<Span>(relaxed = true)

    mockkStatic(Baggage::class)
    mockkStatic(Span::class)
    every { Baggage.current() } returns baggage
    every { baggage.getEntryValue(PROBATION_AREA) } returns "   "
    every { Span.current() } returns span

    filter.doFilter(request, response, chain)

    verifyOrder {
      Baggage.current()
      chain.doFilter(request, response)
    }
    verify { span wasNot Called }
    confirmVerified(span, chain)
  }

  @Test
  fun `does not leak probation area values between requests`() {
    val firstBaggage = mockk<Baggage>()
    val secondBaggage = mockk<Baggage>()
    val firstSpan = mockk<Span>(relaxed = true)
    val secondSpan = mockk<Span>(relaxed = true)
    val firstRequest = mockk<ServletRequest>(relaxed = true)
    val secondRequest = mockk<ServletRequest>(relaxed = true)
    val firstResponse = mockk<ServletResponse>(relaxed = true)
    val secondResponse = mockk<ServletResponse>(relaxed = true)

    mockkStatic(Baggage::class)
    mockkStatic(Span::class)
    every { Baggage.current() } returnsMany listOf(firstBaggage, secondBaggage)
    every { firstBaggage.getEntryValue(PROBATION_AREA) } returns "London"
    every { secondBaggage.getEntryValue(PROBATION_AREA) } returns null
    every { Span.current() } returnsMany listOf(firstSpan, secondSpan)

    filter.doFilter(firstRequest, firstResponse, chain)
    filter.doFilter(secondRequest, secondResponse, chain)

    verifyOrder {
      firstSpan.setAttribute(PROBATION_AREA, "London")
      chain.doFilter(firstRequest, firstResponse)
      chain.doFilter(secondRequest, secondResponse)
    }
    verify { secondSpan wasNot Called }
    confirmVerified(firstSpan, secondSpan, chain)
  }

  private companion object {
    const val PROBATION_AREA = "probationArea"
  }
}
