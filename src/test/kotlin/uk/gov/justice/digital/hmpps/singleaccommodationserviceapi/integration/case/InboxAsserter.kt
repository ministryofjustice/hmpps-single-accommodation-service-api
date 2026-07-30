package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository

@Component
class InboxAsserter(
  val inboxEventRepository: InboxEventRepository,
) {

  fun assertPendingCount(expectedCount: Int) = assertCount(expectedCount, ProcessedStatus.PENDING)

  fun assertProcessedCount(expectedCount: Int) = assertCount(expectedCount, ProcessedStatus.PROCESSED)

  private fun assertCount(expectedCount: Int, status: ProcessedStatus) {
    val processed = inboxEventRepository.findAllByProcessedStatus(status, PageRequest.ofSize(Int.MAX_VALUE))
    assertThat(processed).hasSize(expectedCount)
  }
}
