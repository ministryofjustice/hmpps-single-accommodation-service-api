package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.admin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkRefreshCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReplayFailedInboxEventsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.admin.json.bulkLoadCasesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.admin.json.bulkRefreshCasesByCrnRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.expectApiResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

class AdminJobControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @Autowired
  private lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  private lateinit var inboxEventDispatcher: InboxEventDispatcher

  @Autowired
  private lateinit var clock: MutableTestClock

  private val teamCode = "TEAM1"
  private val teamCases = listOf(
    CaseIdentifiers(crn = "CRN1", prisonerNumber = "PN1"),
    CaseIdentifiers(crn = "CRN2", prisonerNumber = null),
  )
  private val crns = teamCases.map { it.crn }

  private val adminRoles = listOf("ROLE_SAS_ADMIN_RW")

  private val refreshCrn = "X123456"
  private val otherRefreshCrn = "X654321"
  private val now = Instant.parse("2026-07-23T10:00:00Z")

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    clock.freezeAt(now)
    databaseUtils.truncate(DatabaseUtils.SasTables.INBOX_EVENT)
  }

  @AfterEach
  fun teardown() {
    clock.reset()
  }

  @Test
  fun `should return 403 when the client does not have the admin role`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode)))
      .withClientCredentialsJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 if called with Delius user JWT`() {
    createDeliusUser()

    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode)))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should create the cases not already held and stage a bulk refresh for the team`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    assertThat(result.dryRun).isFalse()
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isZero()
    assertThat(result.casesCreated).isEqualTo(2)
    assertThat(result.refreshesRequested).isEqualTo(2)
    assertThat(result.errors).isEmpty()

    assertThat(caseRepository.findUnpersistedCrns(crns.toTypedArray())).isEmpty()

    val persistedCases = caseRepository.findByCrns(crns)
    assertThat(caseRepository.findByCrn("CRN1")?.latestPrisonNumber()).isEqualTo("PN1")
    assertThat(caseRepository.findByCrn("CRN2")?.latestPrisonNumber()).isNull()

    val refreshRequests = caseRefreshRequestRepository.findAll()
    assertThat(refreshRequests.map { it.caseId }).containsExactlyInAnyOrderElementsOf(persistedCases.map { it.id })
    assertThat(refreshRequests).allSatisfy {
      assertThat(it.priority).isEqualTo(CaseRefreshPriority.BULK)
      assertThat(it.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    }
  }

  @Test
  fun `should only count the cases not already held when some of the team is already persisted`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)
    bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isEqualTo(2)
    assertThat(result.casesCreated).isZero()
    assertThat(caseRepository.findByCrns(crns)).hasSize(2)
  }

  @Test
  fun `should report what a run would do without writing anything when dry run is not specified`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(" team1 ")))

    assertThat(result.dryRun).isTrue()
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isZero()
    assertThat(result.casesCreated).isZero()
    assertThat(result.refreshesRequested).isZero()

    assertThat(caseRepository.findByCrns(crns)).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  @Test
  fun `should skip a team whose cases could not be retrieved and report the upstream failure`() {
    SasAndDeliusStubs.stubGetCasesByTeamCodeFailure(teamCode)

    val response = restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchangeSuccessfully()
      .expectApiResponse<BulkLoadCasesResultDto>()

    assertThat(response.data.teamsProcessed).isZero()
    assertThat(response.data.crnsFound).isZero()
    assertThat(response.upstreamFailures).hasSize(1)
    assertThat(response.upstreamFailures.first().failureType).isEqualTo(UpstreamFailureType.UPSTREAM_HTTP_ERROR)
    assertThat(caseRepository.findByCrns(crns)).isEmpty()
  }

  @Test
  fun `should return 400 when no usable team codes are supplied`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf("", "  ")))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchange()
      .expectStatus().isBadRequest

    assertThat(caseRepository.findAll()).isEmpty()
  }

  @Test
  fun `should return 400 when an empty team code list is supplied`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = emptyList()))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 403 when refreshing cases by crn without the admin role`() {
    restTestClient.post().uri("/admin/bulk-refresh-cases-by-crn")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn)))
      .withClientCredentialsJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 when refreshing cases by crn with a Delius user JWT`() {
    createDeliusUser()

    restTestClient.post().uri("/admin/bulk-refresh-cases-by-crn")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn)))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should replay all failed inbox events`() {
    val event = SnsDomainEvent(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED.typeName,
      version = 1,
      occurredAt = OffsetDateTime.now(),
      personReference =
      PersonReference(
        identifiers = listOf(PersonIdentifier("CRN", UUID.randomUUID().toString())),
      ),
    )
    val payload = jsonMapper.writeValueAsString(event)
    val failedFirst = inboxEventRepository.save(
      buildInboxEventEntity(
        eventType = event.eventType,
        payload = payload,
        processedStatus = ProcessedStatus.FAILED,
      ),
    )
    val failedSecond = inboxEventRepository.save(
      buildInboxEventEntity(
        eventType = event.eventType,
        payload = payload,
        processedStatus = ProcessedStatus.FAILED,
      ),
    )
    val ignoredEvent = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.IGNORED))

    val expectedUpdatedCount = inboxEventRepository
      .findAllByProcessedStatus(ProcessedStatus.FAILED, Pageable.unpaged())
      .size

    val response = replayFailedInboxEvents(replayAll = true)

    assertThat(response.replayAll).isTrue()
    assertThat(response.replayedCount).isEqualTo(expectedUpdatedCount)
    assertThat(response.replayedMessageIds).containsExactlyInAnyOrder(failedFirst.id, failedSecond.id)

    val updatedEvents = inboxEventRepository.findAllById(
      listOf(failedFirst.id, failedSecond.id, ignoredEvent.id),
    ).associateBy { it.id }
    assertThat(updatedEvents.getValue(failedFirst.id).processedStatus).isEqualTo(ProcessedStatus.PENDING)
    assertThat(updatedEvents.getValue(failedFirst.id).processedAt).isNull()
    assertThat(updatedEvents.getValue(failedSecond.id).processedStatus).isEqualTo(ProcessedStatus.PENDING)
    assertThat(updatedEvents.getValue(failedSecond.id).processedAt).isNull()
    assertThat(updatedEvents.getValue(ignoredEvent.id).processedStatus).isEqualTo(ProcessedStatus.IGNORED)

    // check the pending messages get picked up and processed.
    inboxEventDispatcher.process()
    waitFor {
      assertThat(inboxEventRepository.findAll()).noneMatch {
        it.processedStatus == ProcessedStatus.PENDING
      }
    }
  }

  @Test
  fun `should replay only the supplied failed inbox events`() {
    val firstFailed = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED))
    val secondFailed = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED))
    val processedEvent = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.PROCESSED))

    val response = replayFailedInboxEvents(inboxEventIds = listOf(firstFailed.id))

    assertThat(response.replayAll).isFalse()
    assertThat(response.replayedCount).isEqualTo(1)
    assertThat(response.replayedMessageIds).containsExactlyInAnyOrder(firstFailed.id)

    val updatedEvents = inboxEventRepository.findAllById(
      listOf(firstFailed.id, secondFailed.id, processedEvent.id),
    ).associateBy { it.id }
    assertThat(updatedEvents.getValue(firstFailed.id).processedStatus).isEqualTo(ProcessedStatus.PENDING)
    assertThat(updatedEvents.getValue(firstFailed.id).processedAt).isNull()
    assertThat(updatedEvents.getValue(secondFailed.id).processedStatus).isEqualTo(ProcessedStatus.FAILED)
    assertThat(updatedEvents.getValue(processedEvent.id).processedStatus).isEqualTo(ProcessedStatus.PROCESSED)
  }

  @Test
  fun `should return 400 when replayAll is false and no inbox event ids are supplied`() {
    val failedEvent = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED))

    buildReplayRequest(false, emptyList())
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage")
      .isEqualTo("Validation failure: To replay selected messages, inboxEventIds must be provided")
      .jsonPath("$.developerMessage").isEqualTo("To replay selected messages, inboxEventIds must be provided")

    assertThat(inboxEventRepository.findById(failedEvent.id).get().processedStatus).isEqualTo(ProcessedStatus.FAILED)
  }

  @Test
  fun `should return 400 when replayAll is true and inbox event ids are supplied`() {
    val failedEvent = inboxEventRepository.save(buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED))
    buildReplayRequest(true, listOf(failedEvent.id))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Validation failure: To replay all messages, inboxEventIds must be empty")
      .jsonPath("$.developerMessage").isEqualTo("To replay all messages, inboxEventIds must be empty")

    assertThat(inboxEventRepository.findById(failedEvent.id).get().processedStatus).isEqualTo(ProcessedStatus.FAILED)
  }

  @Test
  fun `should stage a bulk refresh for the cases holding the crns`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })
    val otherCase = caseRepository.save(buildCaseEntity { withCrn(otherRefreshCrn) })

    val result = bulkRefreshCasesByCrn(
      bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn, otherRefreshCrn), dryRun = false),
    )

    assertThat(result.dryRun).isFalse()
    assertThat(result.crnsRequested).isEqualTo(2)
    assertThat(result.casesFound).isEqualTo(2)
    assertThat(result.refreshesRequested).isEqualTo(2)
    assertThat(result.crnsNotFound).isEmpty()

    val requests = caseRefreshRequestRepository.findAll()
    assertThat(requests.map { it.caseId }).containsExactlyInAnyOrder(case.id, otherCase.id)
    assertThat(requests).allSatisfy {
      assertThat(it.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
      assertThat(it.priority).isEqualTo(CaseRefreshPriority.BULK)
      assertThat(it.attemptCount).isZero()
      assertThat(it.nextAttemptAt).isEqualTo(now)
    }
  }

  @Test
  fun `should refresh the cases it holds and report the crns it does not`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })

    val result = bulkRefreshCasesByCrn(
      bulkRefreshCasesByCrnRequestBody(crns = listOf(" $refreshCrn ", otherRefreshCrn), dryRun = false),
    )

    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.refreshesRequested).isEqualTo(1)
    assertThat(result.crnsNotFound).containsExactly(otherRefreshCrn)

    assertThat(caseRefreshRequestRepository.findAll().single().caseId).isEqualTo(case.id)
  }

  @Test
  fun `should reopen a permanently failed request with a fresh set of attempts`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })
    caseRefreshRequestService.requestBulkRefresh(listOf(case.id))
    val failed = caseRefreshRequestRepository.findAll().single().apply {
      status = CaseRefreshRequestStatus.FAILED
      attemptCount = 3
      nextAttemptAt = null
      failedAt = now
      lastFailureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR
      lastFailureDetail = "getTier: 500 from Tier"
    }
    caseRefreshRequestRepository.saveAndFlush(failed)

    bulkRefreshCasesByCrn(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn), dryRun = false))

    val reopened = caseRefreshRequestRepository.findAll().single()
    assertThat(reopened.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(reopened.attemptCount).isZero()
    assertThat(reopened.nextAttemptAt).isEqualTo(now)
    assertThat(reopened.failedAt).isNull()
    assertThat(reopened.generation).isEqualTo(2)
  }

  @Test
  fun `should bring a request waiting on a retry backoff forward`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })
    caseRefreshRequestService.requestBulkRefresh(listOf(case.id))
    val backingOff = caseRefreshRequestRepository.findAll().single().apply {
      attemptCount = 1
      nextAttemptAt = now.plus(Duration.ofMinutes(30))
    }
    caseRefreshRequestRepository.saveAndFlush(backingOff)

    bulkRefreshCasesByCrn(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn), dryRun = false))

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.nextAttemptAt).isEqualTo(now)
    assertThat(request.attemptCount).isZero()
  }

  @Test
  fun `should leave a request triggered by a live event at live priority`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)

    bulkRefreshCasesByCrn(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn), dryRun = false))

    val request = caseRefreshRequestRepository.findAll().single()
    assertThat(request.priority).isEqualTo(CaseRefreshPriority.LIVE)
    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
  }

  @Test
  fun `should report what a refresh would do without staging anything when dry run is not specified`() {
    caseRepository.save(buildCaseEntity { withCrn(refreshCrn) })

    val result = bulkRefreshCasesByCrn(bulkRefreshCasesByCrnRequestBody(crns = listOf(refreshCrn, otherRefreshCrn)))

    assertThat(result.dryRun).isTrue()
    assertThat(result.crnsRequested).isEqualTo(2)
    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.refreshesRequested).isZero()
    assertThat(result.crnsNotFound).containsExactly(otherRefreshCrn)

    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  @Test
  fun `should return 400 when no usable crns are supplied`() {
    restTestClient.post().uri("/admin/bulk-refresh-cases-by-crn")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkRefreshCasesByCrnRequestBody(crns = listOf("", "  ")))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchange()
      .expectStatus().isBadRequest

    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  private fun bulkRefreshCasesByCrn(requestBody: String) = restTestClient.post().uri("/admin/bulk-refresh-cases-by-crn")
    .contentType(MediaType.APPLICATION_JSON)
    .body(requestBody)
    .withClientCredentialsJwt(roles = adminRoles)
    .exchangeSuccessfully()
    .expectApiResponse<BulkRefreshCasesResultDto>()
    .data

  private fun buildReplayRequest(
    replayAll: Boolean = false,
    inboxEventIds: List<UUID> = emptyList(),
  ) = restTestClient.put().uri("/admin/replay-failed-inbox-events?replayAll=$replayAll")
    .contentType(MediaType.APPLICATION_JSON)
    .body(jsonMapper.writeValueAsString(inboxEventIds))
    .withClientCredentialsJwt(roles = adminRoles)

  private fun replayFailedInboxEvents(
    replayAll: Boolean = false,
    inboxEventIds: List<UUID> = emptyList(),
  ) = buildReplayRequest(replayAll, inboxEventIds)
    .exchangeSuccessfully()
    .expectApiResponse<ReplayFailedInboxEventsResponse>()
    .data

  private fun bulkLoadCases(requestBody: String) = restTestClient.post().uri("/admin/bulk-load-cases")
    .contentType(MediaType.APPLICATION_JSON)
    .body(requestBody)
    .withClientCredentialsJwt(roles = adminRoles)
    .exchangeSuccessfully()
    .expectApiResponse<BulkLoadCasesResultDto>()
    .data
}
