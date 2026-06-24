package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry

import io.mockk.every
import io.mockk.mockk
import io.sentry.SamplingContext
import io.sentry.TransactionContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TracesSamplerCallbackTest {
  private val tracesSampleRate = 0.25
  private val sampler = TracesSamplerCallback(tracesSampleRate)

  @Test
  fun `returns configured trace sample rate when parent transaction is sampled`() {
    val context = samplingContext(parentSampled = true)

    assertThat(sampler.sample(context)).isEqualTo(tracesSampleRate)
  }

  @Test
  fun `returns zero when parent transaction is not sampled`() {
    val context = samplingContext(parentSampled = false)

    assertThat(sampler.sample(context)).isEqualTo(0.0)
  }

  @Test
  fun `returns zero for health checks`() {
    val context = samplingContext(transactionName = "GET /health/readiness")

    assertThat(sampler.sample(context)).isEqualTo(0.0)
  }

  @Test
  fun `returns null for regular requests without a parent transaction`() {
    val context = samplingContext(transactionName = "GET /cases")

    assertThat(sampler.sample(context)).isNull()
  }

  private fun samplingContext(
    transactionName: String = "GET /cases",
    parentSampled: Boolean? = null,
  ): SamplingContext {
    val transactionContext = mockk<TransactionContext> {
      every { name } returns transactionName
      every { this@mockk.parentSampled } returns parentSampled
    }

    return mockk {
      every { this@mockk.transactionContext } returns transactionContext
    }
  }
}
