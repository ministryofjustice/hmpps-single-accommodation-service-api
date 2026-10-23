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

class RequestBaggageTelemetryFilterTest {
  private val filter = RequestBaggageTelemetryFilter()
  private val request = mockk<ServletRequest>(relaxed = true)
  private val response = mockk<ServletResponse>(relaxed = true)
  private val chain = mockk<FilterChain>(relaxed = true)

  private val userLocationCode = "userLocationCode"

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
    every { baggage.getEntryValue(userLocationCode) } returns "London"
    every { Span.current() } returns span

    filter.doFilter(request, response, chain)

    verifyOrder {
      Baggage.current()
      Span.current()
      span.setAttribute(userLocationCode, "London")
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
    every { baggage.getEntryValue(userLocationCode) } returns null
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
    every { baggage.getEntryValue(userLocationCode) } returns "   "
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
    every { firstBaggage.getEntryValue(userLocationCode) } returns "London"
    every { secondBaggage.getEntryValue(userLocationCode) } returns null
    every { Span.current() } returnsMany listOf(firstSpan, secondSpan)

    filter.doFilter(firstRequest, firstResponse, chain)
    filter.doFilter(secondRequest, secondResponse, chain)

    verifyOrder {
      firstSpan.setAttribute(userLocationCode, "London")
      chain.doFilter(firstRequest, firstResponse)
      chain.doFilter(secondRequest, secondResponse)
    }
    verify { secondSpan wasNot Called }
    confirmVerified(firstSpan, secondSpan, chain)
  }
}
