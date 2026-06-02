package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.DispatcherConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

/**
 * Integration tests for [InboxEventDispatcher] proving:
 * - Virtual threads enable concurrent processing
 * - Events are processed in eventOccurredAt order (oldest first)
 * - maxEventsPerBatch limits batch size
 */

@Import(SemaphoreConcurrencyTestConfig::class)
class InboxEventDispatcherIT : IntegrationTestBase() {
  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventDispatcher: InboxEventDispatcher

  @Autowired
  lateinit var jsonMapper: JsonMapper

  @Autowired
  lateinit var dispatcherConfig: DispatcherConfig

  private val crn = UUID.randomUUID().toString()

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate("sas_user", "sas_case", "duty_to_refer", "inbox_event")
    dispatcherConfig.maxConcurrentEvents = 4
    dispatcherConfig.maxEventsPerBatch = 10
  }

  fun createInboxEvent(
    eventOccurredAt: OffsetDateTime,
    crn: String = this.crn,
  ): InboxEventEntity {
    val baseUrl = applicationContext.environment.getProperty("service.tier.base-url")
    val detailUrl = "$baseUrl/crn/$crn/tier"
    val payload =
      SnsDomainEvent(
        eventType = "tier.calculation.complete",
        version = 1,
        description = "Tier calculation complete",
        detailUrl = detailUrl,
        occurredAt = eventOccurredAt,
        personReference =
        PersonReference(
          identifiers = listOf(PersonIdentifier("CRN", crn)),
        ),
      )
    return InboxEventEntity(
      id = UUID.randomUUID(),
      eventType = "tier.calculation.complete",
      eventDetailUrl = detailUrl,
      eventOccurredAt = eventOccurredAt,
      createdAt = Instant.now(),
      processedStatus = ProcessedStatus.PENDING,
      processedAt = null,
      payload = jsonMapper.writeValueAsString(payload),
    )
  }

  @Test
  fun `processes only maxEventsPerBatch events per invocation`() {
    dispatcherConfig.maxEventsPerBatch = 2
    val crns = listOf("X123451", "X123452", "X123453", "X123454", "X123455")
    val caseEntities = crns.map { buildCaseEntity(tierScore = null) { withCrn(it) } }
    caseRepository.saveAll(caseEntities)

    crns.forEach {
      TierStubs.getTierOKResponse(
        it,
        buildTier(tierScore = TierScore.A3),
      )
    }

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      crns.mapIndexed { i, c ->
        createInboxEvent(baseTime.plusSeconds(i.toLong()), crn = c)
      },
    )

    inboxEventDispatcher.process()

    val processed =
      inboxEventRepository.findAllByProcessedStatus(
        ProcessedStatus.PROCESSED,
        Pageable.unpaged(Sort.by("eventOccurredAt").ascending()),
      )
    val pending =
      inboxEventRepository.findAllByProcessedStatus(
        ProcessedStatus.PENDING,
        Pageable.unpaged(Sort.by("eventOccurredAt").ascending()),
      )

    assertThat(processed).hasSize(2)
    assertThat(pending).hasSize(3)
    assertThat(caseRepository.findAll().mapNotNull { it.tierScore }).hasSize(2)
  }

  @Test
  fun `processes events in eventOccurredAt ascending order`() {
    dispatcherConfig.maxEventsPerBatch = 1
    val crns = listOf("X123451", "X123452", "X123453")
    val caseEntities = crns.map { buildCaseEntity(tierScore = null) { withCrn(it) } }
    caseRepository.saveAll(caseEntities)

    crns.forEach {
      TierStubs.getTierOKResponse(
        it,
        buildTier(tierScore = TierScore.A3),
      )
    }

    val t1 = OffsetDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
    val t2 = OffsetDateTime.of(2025, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC)
    val t3 = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)

    inboxEventRepository.saveAll(
      listOf(
        createInboxEvent(t3, crn = crns[2]),
        createInboxEvent(t1, crn = crns[0]),
        createInboxEvent(t2, crn = crns[1]),
      ),
    )

    inboxEventDispatcher.process()
    inboxEventDispatcher.process()
    inboxEventDispatcher.process()

    val processed = inboxEventRepository.findAllByProcessedStatus(
      ProcessedStatus.PROCESSED,
      Pageable.unpaged(Sort.by("eventOccurredAt").ascending()),
    )

    assertThat(processed[0].eventOccurredAt).isEqualTo(t1)
    assertThat(processed[1].eventOccurredAt).isEqualTo(t2)
    assertThat(processed[2].eventOccurredAt).isEqualTo(t3)
    assertThat(caseRepository.findAll()).hasSize(3)
  }

  @Test
  fun `processes concurrent events for same CRN without creating duplicate case rows`() {
    dispatcherConfig.maxEventsPerBatch = 2
    val sharedCrn = "X123456"
    val caseEntity = buildCaseEntity(tierScore = null) { withCrn(sharedCrn) }
    caseRepository.save(caseEntity)

    TierStubs.getTierOKResponse(
      sharedCrn,
      buildTier(tierScore = TierScore.A3),
    )

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      listOf(
        createInboxEvent(baseTime, crn = sharedCrn),
        createInboxEvent(baseTime.plusSeconds(1), crn = sharedCrn),
      ),
    )

    inboxEventDispatcher.process()

    assertThat(
      inboxEventRepository.findAllByProcessedStatus(
        ProcessedStatus.PROCESSED,
        PageRequest.of(0, 10, Sort.by("eventOccurredAt").ascending()),
      ),
    ).hasSize(2)
    assertThat(caseRepository.findAll()).hasSize(1)
    assertThat(
      caseRepository.findByIdentifier(
        sharedCrn,
        IdentifierType.CRN,
      )?.tierScore?.name,
    ).isEqualTo(TierScore.A3.name)
  }

  @Autowired
  lateinit var concurrencyCounter: ConcurrencyCounter

  @Test
  fun `processes at most 4 events concurrently due to semaphore limit`() {
    dispatcherConfig.maxEventsPerBatch = 5
    dispatcherConfig.maxConcurrentEvents = 4
    val crns = (1..5).map { "X12345$it" }

    val caseEntities = crns.map { buildCaseEntity(tierScore = null) { withCrn(it) } }
    caseRepository.saveAll(caseEntities)
    crns.forEach {
      TierStubs.getTierOKResponse(
        it,
        buildTier(tierScore = TierScore.A3),
      )
    }

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      crns.mapIndexed { i, c ->
        createInboxEvent(baseTime.plusSeconds(i.toLong()), crn = c)
      },
    )

    inboxEventDispatcher.process()

    val processed =
      inboxEventRepository.findAllByProcessedStatus(
        ProcessedStatus.PROCESSED,
        PageRequest.of(0, 10, Sort.by("eventOccurredAt").ascending()),
      )
    assertThat(processed).hasSize(5)
    assertThat(caseRepository.findAll()).hasSize(5)

    assertThat(concurrencyCounter.maxConcurrent.get()).isLessThanOrEqualTo(4)
  }

  @Test
  fun `processes multiple events concurrently using coroutines`() {
    val delayMs = 200
    val crns = listOf("X123451", "X123452", "X123453", "X123454")
    val caseEntities = crns.map { buildCaseEntity(tierScore = null) { withCrn(it) } }
    caseRepository.saveAll(caseEntities)

    crns.forEach {
      TierStubs.getTierOKResponse(
        it,
        buildTier(tierScore = TierScore.A3),
        delayMs = delayMs,
      )
    }

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      crns.mapIndexed { i, c ->
        createInboxEvent(baseTime.plusSeconds(i.toLong()), crn = c)
      },
    )

    val start = System.currentTimeMillis()
    inboxEventDispatcher.process()
    val elapsed = System.currentTimeMillis() - start

    val processed =
      inboxEventRepository.findAllByProcessedStatus(
        ProcessedStatus.PROCESSED,
        PageRequest.of(0, 10, Sort.by("eventOccurredAt").ascending()),
      )
    assertThat(processed).hasSize(4)
    assertThat(caseRepository.findAll()).hasSize(4)

    // With 4 events and semaphore(4), parallel execution: ~delayMs. Sequential would be
    // 4*delayMs.
    assertThat(elapsed).isLessThan((delayMs * 3).toLong())
  }
}
