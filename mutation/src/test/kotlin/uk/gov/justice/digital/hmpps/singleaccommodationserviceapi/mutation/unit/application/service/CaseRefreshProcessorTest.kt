package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshCompletionService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailureClassifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProcessor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseRefreshProcessorTest {

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var caseOrchestrationService: CaseMutationOrchestrationService

  @MockK
  lateinit var caseRefreshCompletionService: CaseRefreshCompletionService

  @MockK
  lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @MockK
  lateinit var failureClassifier: CaseRefreshFailureClassifier

  @InjectMockKs
  lateinit var processor: CaseRefreshProcessor

  @Test
  fun `removes owned request when case was archived before it was enqueued`() {
    val claim = CaseRefreshRequestService.Claim(UUID.randomUUID(), 1L, UUID.randomUUID(), CaseRefreshPriority.BULK)
    every { caseRepository.findWithIdentifiersById(claim.caseId) } returns null
    every { caseRefreshRequestService.deleteOwnedRequest(claim) } just runs

    val result = processor.process(claim)

    assertThat(result).isEqualTo(CaseRefreshProcessor.Result.CaseNotFound)
    verify(exactly = 1) { caseRefreshRequestService.deleteOwnedRequest(claim) }
    verify(exactly = 0) { caseRefreshCompletionService.completeRefresh(any(), any()) }
  }
}
