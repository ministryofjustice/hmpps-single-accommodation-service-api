package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.uri
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import java.time.Instant
import java.util.regex.Pattern

@Profile(value = ["local", "dev", "test"])
@Component
class InboxEventProcessor(
  private val inboxEventRepository: InboxEventRepository,
  private val caseApplicationService: CaseApplicationService,

  private val tierClient: TierClient,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(
    name = "InboxEventProcessor",
    lockAtMostFor = "PT2M",
    lockAtLeastFor = "PT1S",
  )
  @Transactional
  fun process() {
    log.info("Start InboxEventProcessor...")
    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING)
    if (inboxEvents.isEmpty()) {
      log.info("No inbox events to process")
      return
    }
    inboxEvents.forEach { inboxEvent ->
      val outgoingHmppsDomainEventType = IncomingHmppsDomainEventType.from(inboxEvent.eventType)
      when (outgoingHmppsDomainEventType) {
        IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE -> {
          log.info("Making callback to Tier using detailUrl ${inboxEvent.eventDetailUrl}")
          try {
            val newTier = tierClient.fetchAddress(uri = inboxEvent.uri())
            log.info("New Tier Score calculated ${newTier.tierScore}")
            val cs: CharSequence = inboxEvent.eventDetailUrl as CharSequence
            val regex = "^.*/crn/(.*)/tier/.*$"

            val crn = Pattern.compile(regex)
              .matcher(cs).group(1)

            caseApplicationService.upsertTier(tier = newTier, crn = crn)

            inboxEvent.processedStatus = ProcessedStatus.SUCCESS
            inboxEvent.processedAt = Instant.now()
            inboxEventRepository.save(inboxEvent)
          } catch (e: Exception) {
            log.error("Failed to process inbox event with id ${inboxEvent.id} exception ${e.message}")
            inboxEvent.processedStatus = ProcessedStatus.FAILED
          }
        }

        else -> log.error("Unexpected event in inbox with inbox event id ${inboxEvent.id} and event type ${inboxEvent.eventType}")
      }
    }
  }
}
