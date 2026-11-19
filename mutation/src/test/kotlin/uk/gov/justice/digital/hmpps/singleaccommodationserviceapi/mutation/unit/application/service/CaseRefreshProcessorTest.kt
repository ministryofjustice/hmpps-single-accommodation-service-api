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
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.*
import java.util.*

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

  @Test
  fun `removes owned request when core person record is missing`() {
    val claim = CaseRefreshRequestService.Claim(UUID.randomUUID(), 1L, UUID.randomUUID(), CaseRefreshPriority.BULK)
    val caseEntity = buildCaseEntity { withCrn("X12345") }
    every { caseRepository.findWithIdentifiersById(claim.caseId) } returns caseEntity
    every { caseOrchestrationService.getCurrentCaseResult("X12345", any()) } returns OrchestrationResultDto(
      data = CaseMutationOrchestrationDto(
        crn = "X12345",
        cpr = null,
        tier = null,
        prisoner = null,
        cas1Residence = null,
        cas3CurrentPremises = null,
        cas1Application = null,
        cas3Application = null,
        case = null,
      ),
      upstreamFailures = listOf(
        UpstreamFailure(
          callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN,
          type = FailureType.UPSTREAM_HTTP_ERROR,
          errorDetail = ErrorDetail(HttpStatus.NOT_FOUND, "404 Not Found"),
        ),
      ),
    )
    every { failureClassifier.classify(any()) } returns CaseRefreshFailure(
      category = CaseRefreshFailureCategory.CORE_PERSON_RECORD_NOT_FOUND,
      detail = "getCorePersonRecordByCrn: 404 Not Found",
    )
    every { caseRefreshRequestService.deleteOwnedRequest(claim) } just runs

    val result = processor.process(claim)

    assertThat(result).isEqualTo(CaseRefreshProcessor.Result.CaseNotFound)
    verify(exactly = 1) { caseRefreshRequestService.deleteOwnedRequest(claim) }
    verify(exactly = 0) { caseRefreshRequestService.recordFailure(any(), any()) }
  }
}
