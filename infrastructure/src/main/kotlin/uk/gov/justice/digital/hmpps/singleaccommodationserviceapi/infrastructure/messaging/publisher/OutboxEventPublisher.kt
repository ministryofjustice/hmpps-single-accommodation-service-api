package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.publisher

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.HmppsDomainEventUrlConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsSnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.ZoneOffset

@Profile(value = ["local", "dev", "test"])
@Component
class OutboxEventPublisher(
  private val jsonMapper: JsonMapper,
  private val hmppsDomainEventUrlConfig: HmppsDomainEventUrlConfig,
  private val outboxEventRepository: OutboxEventRepository,
  private val hmppsQueueService: HmppsQueueService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic") ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(
    name = "OutboxEventPublisher",
    lockAtMostFor = "PT2M",
    lockAtLeastFor = "PT1S",
  )
  @Transactional
  fun publish() {
    log.info("Start OutboxEventPublisher...")
    val outboxEventsToPublish = outboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING)
    if (outboxEventsToPublish.isEmpty()) {
      log.info("No events to publish")
      return
    }
    outboxEventsToPublish.forEach {
      val eventType = SingleAccommodationServiceDomainEventType.from(it.domainEventType)!!
      val publishResult = publishHmppsDomainEvent(outboxEventEntity = it, eventType)
      log.info("Emitted SNS event (Message Id: ${publishResult.messageId()}, Sequence Id: ${publishResult.sequenceNumber()}) for Outbox Event: ${it.id} of type: $eventType")
      outboxEventRepository.save(
        it.copy(processedStatus = ProcessedStatus.SUCCESS),
      )
    }
  }

  private fun publishHmppsDomainEvent(
    outboxEventEntity: OutboxEventEntity,
    eventType: SingleAccommodationServiceDomainEventType,
  ): PublishResponse {
    val detailUrl = hmppsDomainEventUrlConfig.getUrlForDomainEventId(eventType, outboxEventEntity.aggregateId)
    val snsEvent = HmppsSnsDomainEvent(
      eventType = eventType.typeName,
      externalId = outboxEventEntity.aggregateId,
      detailUrl = detailUrl,
      version = 1,
      description = eventType.typeDescription,
      occurredAt = outboxEventEntity.createdAt.atOffset(ZoneOffset.UTC),
    )

    return domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(jsonMapper.writeValueAsString(snsEvent))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(snsEvent.eventType).build(),
          ),
        ).build(),
    ).get()
  }
}
