package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.WebClientRetry

class CorePersonRecordClientRetryAnnotationsTest {

  @Test
  fun `createProbationAddress in client is not retryable`() {
    val method = CorePersonRecordClient::class.java.getMethod(
      "createProbationAddress",
      String::class.java,
      ProbationCreateAddress::class.java,
    )

    assertThat(method.isAnnotationPresent(WebClientRetry::class.java)).isFalse()
  }

  @Test
  fun `createProbationAddress in caching service is not retryable`() {
    val method = CorePersonRecordCachingService::class.java.getMethod(
      "createProbationAddress",
      String::class.java,
      ProbationCreateAddress::class.java,
    )

    assertThat(method.isAnnotationPresent(WebClientRetry::class.java)).isFalse()
  }
}
