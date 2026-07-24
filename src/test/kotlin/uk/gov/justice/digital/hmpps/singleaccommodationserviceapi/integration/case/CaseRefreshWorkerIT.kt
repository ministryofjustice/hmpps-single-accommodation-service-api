package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshWorker
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@TestPropertySource(
  properties = [
    "case-refresh.worker.enabled=true",
    "case-refresh.worker.max-attempts=2",
    "case-refresh.worker.initial-backoff=1m",
    "case-refresh.worker.max-backoff=5m",
    "case-refresh.worker.abandoned-claim-timeout=10m",
  ],
)
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
    caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.AWAITING_PLACEMENT,
        cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.AWAITING_MATCH,
        cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
      ) {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      },
    )
    caseRefreshRequestService.requestLiveRefresh(crn)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"))
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      placementStatus = Cas1PlacementStatus.ARRIVED,
    )
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn, cas1Application)
    PrisonerSearchStubs.getPrisonerOKResponse(prisonNumber, buildPrisoner(prisonNumber))

    caseRefreshWorker.process()

    val refreshedCase = caseRepository.findByCrn(crn)!!
    assertThat(refreshedCase.tierScore).isEqualTo("A3")
    assertThat(refreshedCase.cas1ApplicationId).isEqualTo(cas1Application.id)
    assertThat(refreshedCase.cas1ApplicationApplicationStatus).isEqualTo(cas1Application.applicationStatus)
    assertThat(refreshedCase.cas1ApplicationRequestForPlacementStatus).isEqualTo(cas1Application.requestForPlacementStatus)
    assertThat(refreshedCase.cas1ApplicationPlacementStatus).isEqualTo(cas1Application.placementStatus)
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/v2/crn/$crn/tier")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/person/probation/$crn")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/cas1/external/cases/$crn/premises/current")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/cas3/external/cases/$crn/premises/current")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/cas1/external/cases/$crn/applications/suitable")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/cas3/external/cases/$crn/applications/suitable")))
    sasWiremock.verify(1, getRequestedFor(urlPathEqualTo("/prisoner/$prisonNumber")))
  }

  @Test
  fun `retains the previous projection and refresh request when an upstream service fails`() {
    val originalApplicationId = UUID.randomUUID()
    caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
        cas1ApplicationId = originalApplicationId,
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      ) { withCrn(crn) },
    )
    caseRefreshRequestService.requestLiveRefresh(crn)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"))
    ApprovedPremisesStubs.getCas1SuitableApplicationServerErrorResponse(crn)

    val result = caseRefreshWorker.process()

    val unchangedCase = caseRepository.findByCrn(crn)!!
    assertThat(unchangedCase.tierScore).isEqualTo("A1")
    assertThat(unchangedCase.cas1ApplicationId).isEqualTo(originalApplicationId)
    assertThat(unchangedCase.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.AWAITING_ASSESSMENT)
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
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(crn)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"), delayMs = 200)

    val workerRun = CompletableFuture.runAsync { caseRefreshWorker.process() }
    waitFor {
      assertThat(caseRefreshRequestRepository.findAll().single().status)
        .isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    }

    caseRefreshRequestService.requestLiveRefresh(crn)
    workerRun.get(5, TimeUnit.SECONDS)

    val retainedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(caseRepository.findByCrn(crn)!!.tierScore).isEqualTo("A3")
    assertThat(retainedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(retainedRequest.generation).isEqualTo(2)
  }

  @Test
  fun `refreshes all supported fields when the Case changes during upstream loading`() {
    caseRepository.save(
      buildCaseEntity(
        tierScore = "A1",
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      ) { withCrn(crn) },
    )
    caseRefreshRequestService.requestLiveRefresh(crn)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"), delayMs = 200)
    ApprovedPremisesStubs.getCas1SuitableApplicationNotFoundResponse(crn)

    val workerRun = CompletableFuture.runAsync { caseRefreshWorker.process() }
    waitFor {
      assertThat(caseRefreshRequestRepository.findAll().single().status)
        .isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    }
    val concurrentlyUpdatedCase = caseRepository.findByCrn(crn)!!
    concurrentlyUpdatedCase.cas1ApplicationApplicationStatus = Cas1ApplicationStatus.REJECTED
    caseRepository.saveAndFlush(concurrentlyUpdatedCase)
    workerRun.get(5, TimeUnit.SECONDS)

    val refreshedCase = caseRepository.findByCrn(crn)!!
    assertThat(refreshedCase.tierScore).isEqualTo("A3")
    assertThat(refreshedCase.cas1ApplicationApplicationStatus).isNull()
  }

  @Test
  fun `atomically coalesces concurrent refresh requests`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    val requests = (1..5).map {
      CompletableFuture.runAsync { caseRefreshRequestService.requestLiveRefresh(crn) }
    }
    CompletableFuture.allOf(*requests.toTypedArray()).get(5, TimeUnit.SECONDS)

    val refreshRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refreshRequest.generation).isEqualTo(5)
    assertThat(refreshRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
  }

  @Test
  fun `allows only one concurrent worker to claim a Case`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(crn)

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
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(crn)
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = "A3"))
    ApprovedPremisesStubs.getCas1SuitableApplicationServerErrorResponse(crn)

    caseRefreshWorker.process()
    clock.freezeAt(now.plus(Duration.ofMinutes(1)))
    caseRefreshWorker.process()

    val terminalRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(terminalRequest.status).isEqualTo(CaseRefreshRequestStatus.FAILED)
    assertThat(terminalRequest.attemptCount).isEqualTo(2)
    assertThat(terminalRequest.nextAttemptAt).isNull()
    assertThat(terminalRequest.failedAt).isEqualTo(now.plus(Duration.ofMinutes(1)))

    clock.freezeAt(now.plus(Duration.ofMinutes(2)))
    caseRefreshRequestService.requestLiveRefresh(crn)

    val reopenedRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(reopenedRequest.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(reopenedRequest.attemptCount).isZero()
    assertThat(reopenedRequest.nextAttemptAt).isEqualTo(now.plus(Duration.ofMinutes(2)))
    assertThat(reopenedRequest.lastFailureCategory).isNull()
    assertThat(reopenedRequest.failedAt).isNull()
  }

  @Test
  fun `reclaims abandoned work with a new token and rejects the original claimant`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(crn)
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
          cas1Application = null,
        ),
      ),
    ).isEqualTo(CaseRefreshCompletionService.Result.IgnoredStaleClaim)

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.claimId).isEqualTo(replacementClaim.claimId)
    assertThat(caseRepository.findByCrn(crn)!!.tierScore).isEqualTo("A1")
  }
}
