package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.currentOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.sortedRequestForPlacementHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.statusLastUpdated
import java.time.LocalDateTime

class Cas1ApplicationV2ExtensionsTest {
  private val now = LocalDateTime.of(2026, 1, 10, 12, 0)

  private fun outcome(requestUpdated: LocalDateTime, placementUpdated: LocalDateTime? = null) = buildCas1ExternalRequestForPlacementOutcomeDto(
    requestForPlacement = buildCas1RequestForPlacementSummaryV2(statusLastUpdated = requestUpdated),
    placement = placementUpdated?.let { buildCas1PlacementV2(statusLastUpdated = it) },
  )

  @Nested
  inner class OutcomeStatusLastUpdated {
    @Test
    fun `is the request for placement time when there is no placement`() {
      assertThat(outcome(requestUpdated = now).statusLastUpdated).isEqualTo(now)
    }

    @Test
    fun `is the placement time when it is later than the request time`() {
      assertThat(outcome(requestUpdated = now, placementUpdated = now.plusDays(1)).statusLastUpdated)
        .isEqualTo(now.plusDays(1))
    }

    @Test
    fun `is the request time when it is later than the placement time`() {
      assertThat(outcome(requestUpdated = now.plusDays(1), placementUpdated = now).statusLastUpdated)
        .isEqualTo(now.plusDays(1))
    }
  }

  @Nested
  inner class SortedRequestForPlacementHistory {
    @Test
    fun `sorts by most recently updated first regardless of supplied order`() {
      val oldest = outcome(requestUpdated = now.minusDays(3))
      val middle = outcome(requestUpdated = now.minusDays(2))
      val newestViaPlacement = outcome(requestUpdated = now.minusDays(5), placementUpdated = now)

      val application = buildCas1ApplicationV2(
        requestForPlacementHistory = listOf(middle, newestViaPlacement, oldest),
      )

      assertThat(application.sortedRequestForPlacementHistory)
        .containsExactly(newestViaPlacement, middle, oldest)
    }
  }

  @Nested
  inner class CurrentOutcome {
    @Test
    fun `is the upcoming outcome when present`() {
      val upcoming = outcome(requestUpdated = now.minusDays(10))
      val application = buildCas1ApplicationV2(
        upcomingRequestForPlacement = upcoming,
        requestForPlacementHistory = listOf(outcome(requestUpdated = now)),
      )

      assertThat(application.currentOutcome).isEqualTo(upcoming)
    }

    @Test
    fun `is the most recently updated historic outcome when there is no upcoming outcome`() {
      val latest = outcome(requestUpdated = now)
      val application = buildCas1ApplicationV2(
        requestForPlacementHistory = listOf(outcome(requestUpdated = now.minusDays(1)), latest),
      )

      assertThat(application.currentOutcome).isEqualTo(latest)
    }

    @Test
    fun `is null when there is no upcoming or historic outcome`() {
      assertThat(buildCas1ApplicationV2().currentOutcome).isNull()
    }
  }
}
