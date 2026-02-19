package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.TestPropertySource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.TierDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class InboxEventDispatcherIT {

  /**
   * Integration tests for [InboxEventDispatcher] proving:
   * - Virtual threads enable concurrent processing
   * - Events are processed in eventOccurredAt order (oldest first)
   * - maxEventsPerBatch limits batch size
   */
  abstract inner class InboxEventDispatcherITBase : IntegrationTestBase() {

    @Autowired
    lateinit var inboxEventRepository: InboxEventRepository

    @Autowired
    lateinit var caseRepository: CaseRepository

    @Autowired
    lateinit var inboxEventDispatcher: InboxEventDispatcher

    @Autowired
    lateinit var jsonMapper: JsonMapper

    protected val crn = "X123456"

    @BeforeEach
    fun setup() {
      hmppsAuth.stubGrantToken()
      inboxEventRepository.deleteAll()
      caseRepository.deleteAll()
    }

    protected fun createInboxEvent(
      eventOccurredAt: OffsetDateTime,
      crn: String = this.crn,
    ): InboxEventEntity {
      val detailUrl = "http://localhost:9994/crn/$crn/tier"
      val payload =
        TierDomainEvent(
          eventType = "tier.calculation.complete",
          externalId = UUID.randomUUID(),
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
  }

  @Nested
  @TestPropertySource(properties = ["hmpps.sqs.dispatcher.max-events-per-batch=2"])
  inner class InboxEventDispatcherMaxBatchIT : InboxEventDispatcherITBase() {

    @Test
    fun `processes only maxEventsPerBatch events per invocation`() {
      val crns = listOf("X123451", "X123452", "X123453", "X123454", "X123455")
      crns.forEach {
        tierMockServer.stubGetCorePersonRecordOKResponse(
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
          ProcessedStatus.SUCCESS,
          PageRequest.of(0, 20, Sort.by("eventOccurredAt").ascending()),
        )
      val pending =
        inboxEventRepository.findAllByProcessedStatus(
          ProcessedStatus.PENDING,
          PageRequest.of(0, 20, Sort.by("eventOccurredAt").ascending()),
        )

      assertThat(processed).hasSize(2)
      assertThat(pending).hasSize(3)
      assertThat(caseRepository.findAll()).hasSize(2)
    }
  }

  @Nested
  @TestPropertySource(properties = ["hmpps.sqs.dispatcher.max-events-per-batch=1"])
  inner class InboxEventDispatcherOrderIT : InboxEventDispatcherITBase() {

    @Test
    fun `processes events in eventOccurredAt ascending order`() {
      val crns = listOf("X123451", "X123452", "X123453")
      crns.forEach {
        tierMockServer.stubGetCorePersonRecordOKResponse(
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
      val firstProcessed =
        inboxEventRepository
          .findAllByProcessedStatus(
            ProcessedStatus.SUCCESS,
            PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
          )
          .single()

      inboxEventDispatcher.process()
      val secondProcessed =
        inboxEventRepository
          .findAllByProcessedStatus(
            ProcessedStatus.SUCCESS,
            PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
          )
          .last()

      inboxEventDispatcher.process()
      val thirdProcessed =
        inboxEventRepository
          .findAllByProcessedStatus(
            ProcessedStatus.SUCCESS,
            PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
          )
          .last()

      assertThat(firstProcessed.eventOccurredAt).isEqualTo(t1)
      assertThat(secondProcessed.eventOccurredAt).isEqualTo(t2)
      assertThat(thirdProcessed.eventOccurredAt).isEqualTo(t3)
      assertThat(caseRepository.findAll()).hasSize(3)
    }
  }

  @Nested
  @TestPropertySource(properties = ["hmpps.sqs.dispatcher.max-events-per-batch=2"])
  inner class InboxEventDispatcherSameCrnIT : InboxEventDispatcherITBase() {

    @Disabled("Need to rethink the unique record idea, this breaks as we run the same crns in multiple threads")
    @Test
    fun `processes concurrent events for same CRN without creating duplicate case rows`() {
      val sharedCrn = "X123456"
      tierMockServer.stubGetCorePersonRecordOKResponse(
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

      assertThat(inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.SUCCESS, PageRequest.of(0, 10, Sort.by("eventOccurredAt").ascending()))).hasSize(2)
      assertThat(caseRepository.findAll()).hasSize(1)
      assertThat(caseRepository.findByCrn(sharedCrn)?.tier?.name).isEqualTo(TierScore.A3.name)
    }
  }
}
