package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import java.util.concurrent.CopyOnWriteArrayList

class AggregatorServiceTest {
  private val upstreamFailureReporter = mockk<UpstreamFailureReporter>(relaxed = true)
  private val aggregatorService = AggregatorService(listOf(upstreamFailureReporter))

  @Test
  fun `reports handled async failures without failing the whole aggregation`() {
    val exception = IllegalArgumentException("upstream unavailable")

    val result = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = mapOf(
        "workingCall" to { "success" },
        "failingCall" to { throw exception },
      ),
    )

    assertThat(result.standardCallsNoIterationResults?.getResult<String>("workingCall")).isEqualTo("success")
    assertThat(result.standardCallsNoIterationResults?.getFailures()).containsExactly(
      UpstreamFailure(
        callKey = "failingCall",
        type = FailureType.UNKNOWN_ERROR,
        errorDetail = ErrorDetail(message = "upstream unavailable"),
      ),
    )
    verify(exactly = 1) {
      upstreamFailureReporter.report(
        "failingCall",
        AggregatorCallOutcome.Failure(
          type = FailureType.UNKNOWN_ERROR,
          errorDetail = ErrorDetail(message = "upstream unavailable"),
        ),
        exception,
      )
    }
  }

  @Nested
  inner class FetchAllPages {
    private val fetchedPages = CopyOnWriteArrayList<Long>()

    private fun fetchAllPages(fetchPage: (Long) -> PagedItems) = aggregatorService.fetchAllPages(
      callKey = { page -> "callKey$page" },
      fetchPage = { page ->
        fetchedPages.add(page)
        fetchPage(page)
      },
      pageMetadata = PagedItems::page,
      content = PagedItems::items,
    )

    private fun buildPage(number: Long, totalPages: Long, items: List<String> = listOf("item$number")) = PagedItems(
      items = items,
      page = PageMetadata(size = 1, number = number, totalElements = totalPages, totalPages = totalPages),
    )

    private fun buildFailure(page: Long) = UpstreamFailure(
      callKey = "callKey$page",
      type = FailureType.UNKNOWN_ERROR,
      errorDetail = ErrorDetail(message = "page $page failed"),
    )

    @Test
    fun `returns no data when there are no pages`() {
      val result = fetchAllPages { buildPage(number = 0, totalPages = 0, items = emptyList()) }

      assertThat(result.data).isEmpty()
      assertThat(result.upstreamFailures).isEmpty()
      assertThat(fetchedPages).containsExactly(0)
    }

    @Test
    fun `makes one call when there is a single page`() {
      val result = fetchAllPages { page -> buildPage(page, totalPages = 1) }

      assertThat(result.data).containsExactly("item0")
      assertThat(result.upstreamFailures).isEmpty()
      assertThat(fetchedPages).containsExactly(0)
    }

    @Test
    fun `returns the content of every page in page order`() {
      val result = fetchAllPages { page -> buildPage(page, totalPages = 3) }

      assertThat(result.data).containsExactly("item0", "item1", "item2")
      assertThat(result.upstreamFailures).isEmpty()
      assertThat(fetchedPages).containsExactlyInAnyOrder(0, 1, 2)
    }

    @Test
    fun `returns only a failure and fetches no more pages when the first page fails`() {
      val result = fetchAllPages { page -> throw IllegalArgumentException("page $page failed") }

      assertThat(result.data).isEmpty()
      assertThat(result.upstreamFailures).containsExactly(buildFailure(page = 0))
      assertThat(fetchedPages).containsExactly(0)
    }

    @Test
    fun `returns the other pages and a failure when a later page fails`() {
      val result = fetchAllPages { page ->
        if (page == 1L) throw IllegalArgumentException("page $page failed")
        buildPage(page, totalPages = 3)
      }

      assertThat(result.data).containsExactly("item0", "item2")
      assertThat(result.upstreamFailures).containsExactly(buildFailure(page = 1))
    }
  }

  private data class PagedItems(val items: List<String>, val page: PageMetadata)
}
