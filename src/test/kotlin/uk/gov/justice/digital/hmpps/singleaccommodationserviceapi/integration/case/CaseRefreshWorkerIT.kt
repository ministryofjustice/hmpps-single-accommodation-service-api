package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.PrisonerSearchStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshCompletionService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshWorker
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class CaseRefreshWorkerIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @Autowired
  lateinit var caseRefreshWorker: CaseRefreshWorker

  @Autowired
  lateinit var caseRefreshCompletionService: CaseRefreshCompletionService

  @Autowired
  lateinit var clock: MutableTestClock

  private lateinit var crn: String
  private val now = Instant.parse("2026-07-23T10:00:00Z")

  @BeforeEach
  fun setup() {
    clock.freezeAt(now)
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, buildCorePersonRecord())
    createSasSystemUser()
  }

  @AfterEach
  fun teardown() {
    clock.reset()
  }

  @Test
  fun `refreshes the full Case projection`() {
    val prisonNumber = "A1234AA"
    val case = caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
      ) {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      },
    )
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"))
    PrisonerSearchStubs.getPrisonerOKResponse(prisonNumber, buildPrisoner(prisonNumber))

    caseRefreshWorker.process()

    val refreshedCase = caseRepository.findByCrn(crn)!!
    assertThat(refreshedCase.tierScore).isEqualTo("A3")
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/v2/crn/$crn/tier")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/person/probation/$crn")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/prisoner/$prisonNumber")))
  }

  @Test
  fun `retains the previous projection and refresh request when an upstream service fails`() {
    val case = caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
      ) { withCrn(crn) },
    )
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierServerErrorResponse(crn)
    ApprovedPremisesStubs.getCas1SuitableApplicationServerErrorResponse(crn)

    val result = caseRefreshWorker.process()

    val unchangedCase = caseRepository.findByCrn(crn)!!
    assertThat(unchangedCase.tierScore).isEqualTo("A1")
    assertThat(caseRefreshRequestRepository.findAll()).hasSize(1)
    assertThat(result.refreshedCount).isZero()
    assertThat(result.failedCount).isEqualTo(1)
    val failedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(failedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(failedRequest.attemptCount).isEqualTo(1)
    assertThat(failedRequest.nextAttemptAt).isEqualTo(now.plus(Duration.ofMinutes(1)))
    assertThat(failedRequest.lastFailureCategory).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)

    val immediateRetry = caseRefreshWorker.process()

    assertThat(immediateRetry.refreshedCount).isZero()
    assertThat(immediateRetry.failedCount).isZero()
  }

  @Test
  fun `retains a newer refresh request that arrives while Tier is loading`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"), delayMs = 200)

    val workerRun = CompletableFuture.runAsync { caseRefreshWorker.process() }
    waitFor {
      assertThat(caseRefreshRequestRepository.findAll().single().status)
        .isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    }

    caseRefreshRequestService.requestLiveRefresh(case.id)
    workerRun.get(5, TimeUnit.SECONDS)

    val retainedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(caseRepository.findByCrn(crn)!!.tierScore).isEqualTo("A3")
    assertThat(retainedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(retainedRequest.generation).isEqualTo(2)
  }

  @Test
  fun `refreshes all supported fields when the Case changes during upstream loading`() {
    val case = caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
      ) { withCrn(crn) },
    )
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"), delayMs = 200)
    ApprovedPremisesStubs.getCas1SuitableApplicationNotFoundResponse(crn)

    val workerRun = CompletableFuture.runAsync { caseRefreshWorker.process() }
    waitFor {
      assertThat(caseRefreshRequestRepository.findAll().single().status)
        .isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    }
    val concurrentlyUpdatedCase = caseRepository.findByCrn(crn)!!
    caseRepository.saveAndFlush(concurrentlyUpdatedCase)
    workerRun.get(5, TimeUnit.SECONDS)

    val refreshedCase = caseRepository.findByCrn(crn)!!
    assertThat(refreshedCase.tierScore).isEqualTo("A3")
  }

  @Test
  fun `atomically coalesces concurrent refresh requests`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    val requests = (1..5).map {
      CompletableFuture.runAsync { caseRefreshRequestService.requestLiveRefresh(case.id) }
    }
    CompletableFuture.allOf(*requests.toTypedArray()).get(5, TimeUnit.SECONDS)

    val refreshRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refreshRequest.generation).isEqualTo(5)
    assertThat(refreshRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
  }

  @Test
  fun `allows only one concurrent worker to claim a Case`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)

    val claimAttempts = (1..2).map {
      CompletableFuture.supplyAsync {
        caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10))
      }
    }
    val claims = claimAttempts.flatMap { it.get(5, TimeUnit.SECONDS) }

    assertThat(claims).hasSize(1)
    assertThat(caseRefreshRequestRepository.findAll().single().status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
  }

  @Test
  fun `moves repeatedly failing work to terminal failure and reopens it for a new event`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierServerErrorResponse(crn)

    caseRefreshWorker.process()
    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    caseRefreshWorker.process()

    val terminalRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(terminalRequest.status).isEqualTo(CaseRefreshRequestStatus.FAILED)
    assertThat(terminalRequest.attemptCount).isEqualTo(2)
    assertThat(terminalRequest.nextAttemptAt).isNull()
    assertThat(terminalRequest.failedAt).isEqualTo(now.plus(Duration.ofMinutes(1)))

    clock.freezeAt(now.plus(Duration.ofMinutes(2)))
    caseRefreshRequestService.requestLiveRefresh(case.id)

    val reopenedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(reopenedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(reopenedRequest.attemptCount).isEqualTo(2)
    assertThat(reopenedRequest.nextAttemptAt).isEqualTo(now.plus(Duration.ofMinutes(2)))
    assertThat(reopenedRequest.lastFailureCategory).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)
    assertThat(reopenedRequest.failedAt).isNull()

    caseRefreshWorker.process()

    val refailedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refailedRequest.status).isEqualTo(CaseRefreshRequestStatus.FAILED)
    assertThat(refailedRequest.attemptCount).isEqualTo(3)
    assertThat(refailedRequest.failedAt).isEqualTo(now.plus(Duration.ofMinutes(2)))
  }

  @Test
  fun `retains the backoff and attempt count when a new event arrives for failing work`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierServerErrorResponse(crn)

    caseRefreshWorker.process()

    clock.freezeAt(now.plus(Duration.ofSeconds(10)))
    caseRefreshRequestService.requestLiveRefresh(case.id)

    val churnedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(churnedRequest.generation).isEqualTo(2)
    assertThat(churnedRequest.attemptCount).isEqualTo(1)
    assertThat(churnedRequest.nextAttemptAt).isEqualTo(now.plus(Duration.ofMinutes(1)))
    assertThat(churnedRequest.requestedAt).isEqualTo(now)
    assertThat(churnedRequest.lastFailureCategory).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)

    val duringBackoff = caseRefreshWorker.process()

    assertThat(duringBackoff.refreshedCount).isZero()
    assertThat(duringBackoff.failedCount).isZero()

    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    caseRefreshWorker.process()

    assertThat(caseRefreshRequestRepository.findAll().single().status)
      .isEqualTo(CaseRefreshRequestStatus.FAILED)
  }

  @Test
  fun `clears the failure history when a refresh succeeds while a newer event is waiting`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)

    val failedClaim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()
    caseRefreshRequestService.recordFailure(
      failedClaim,
      CaseRefreshFailure(
        category = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
        detail = "500 server error",
      ),
    )
    assertThat(caseRefreshRequestRepository.findAll().single().attemptCount).isEqualTo(1)

    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"), delayMs = 200)
    ApprovedPremisesStubs.getCas1SuitableApplicationNotFoundResponse(crn)

    val workerRun = CompletableFuture.runAsync { caseRefreshWorker.process() }
    waitFor {
      assertThat(caseRefreshRequestRepository.findAll().single().status)
        .isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    }

    caseRefreshRequestService.requestLiveRefresh(case.id)
    workerRun.get(5, TimeUnit.SECONDS)

    val retainedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(caseRepository.findByCrn(crn)!!.tierScore).isEqualTo("A3")
    assertThat(retainedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(retainedRequest.generation).isEqualTo(2)
    assertThat(retainedRequest.attemptCount).isZero()
    assertThat(retainedRequest.lastFailureCategory).isNull()
    assertThat(retainedRequest.lastFailureDetail).isNull()
  }

  @Test
  fun `claims live work ahead of bulk work that has been waiting longer`() {
    val bulkCase = caseRepository.save(
      buildCaseEntity(tierScore = "A1") { withCrn(UUID.randomUUID().toString()) },
    )
    caseRefreshRequestService.requestBulkRefresh(listOf(bulkCase.id))

    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    val liveCase = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(liveCase.id)

    clock.freezeAt(now.plus(Duration.ofMinutes(2)))
    val firstClaim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()
    val secondClaim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()

    assertThat(firstClaim.caseId).isEqualTo(liveCase.id)
    assertThat(secondClaim.caseId).isEqualTo(bulkCase.id)
  }

  @Test
  fun `bulk preload leaves an existing refresh request untouched`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)

    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    caseRefreshRequestService.requestBulkRefresh(listOf(case.id))

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.priority).isEqualTo(CaseRefreshPriority.LIVE)
    assertThat(request.generation).isEqualTo(1)
    assertThat(request.requestedAt).isEqualTo(now)
    assertThat(request.nextAttemptAt).isEqualTo(now)
  }

  @Test
  fun `bulk preload does not reopen terminally failed work`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    TierStubs.getTierServerErrorResponse(crn)

    caseRefreshWorker.process()
    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    caseRefreshWorker.process()
    assertThat(caseRefreshRequestRepository.findAll().single().status)
      .isEqualTo(CaseRefreshRequestStatus.FAILED)

    clock.freezeAt(now.plus(Duration.ofMinutes(2)))
    caseRefreshRequestService.requestBulkRefresh(listOf(case.id))

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.FAILED)
    assertThat(request.attemptCount).isEqualTo(2)
  }

  @Test
  fun `reclaims abandoned work with a new token and rejects the original claimant`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    val originalClaim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()

    clock.freezeAt(now.plus(Duration.ofMinutes(11)))
    val replacementClaim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()

    assertThat(replacementClaim.claimId).isNotEqualTo(originalClaim.claimId)
    assertThat(
      caseRefreshCompletionService.completeRefresh(
        originalClaim,
        CaseMutationOrchestrationDto(
          crn = crn,
          cpr = null,
          tier = buildTier(tierScore = "A9"),
          prisoner = null,
        ),
      ),
    ).isEqualTo(CaseRefreshCompletionService.Result.IGNORED_STALE_CLAIM)

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.claimId).isEqualTo(replacementClaim.claimId)
    assertThat(caseRepository.findByCrn(crn)!!.tierScore).isEqualTo("A1")
  }
}
