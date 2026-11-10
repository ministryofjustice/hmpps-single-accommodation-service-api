package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.listener

import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.LambdaDslObject
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.consumer.junit5.ProviderType
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_CREATED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_DELETED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_UPDATED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_MERGED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import java.time.OffsetDateTime
import java.util.UUID

private const val CONSUMER = "hmpps-single-accommodation-service-api"
private const val PROVIDER = "hmpps-person-record-events"
private const val CRN_PATTERN = "[A-Z]\\d{6}"
private const val UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = PROVIDER, providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4)
class CorePersonRecordEventPactTest {
  private val crn = "X123456"
  private val cprAddressId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001")
  private val occurredAt = "2026-07-24T13:58:28.076572456+01:00"
  private val baseUrl = "https://hmpps-person-record.hmpps.service.justice.gov.uk"
  private val personDetailUrl = "$baseUrl/person/probation/$crn"
  private val addressDetailUrl = "$personDetailUrl/address/$cprAddressId"

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationAddressCreated(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_ADDRESS_CREATED) { body ->
    body.stringType("description", "A probation address has been created for a person")
    body.stringMatcher("detailUrl", "^https?://[^/\\s]+/person/probation/$CRN_PATTERN/address/$UUID_PATTERN$", addressDetailUrl)
    personReference(body, "CRN")
    addressInformation(body)
  }

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationAddressUpdated(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_ADDRESS_UPDATED) { body ->
    body.stringType("description", "A probation address has been updated for a person")
    body.stringMatcher("detailUrl", "^https?://[^/\\s]+/person/probation/$CRN_PATTERN/address/$UUID_PATTERN$", addressDetailUrl)
    personReference(body, "CRN")
    addressInformation(body)
  }

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationAddressDeleted(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_ADDRESS_DELETED) { body ->
    body.stringType("description", "A probation address has been deleted for a person")
    personReference(body, "CRN")
    addressInformation(body)
  }

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationRecordUpdated(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_RECORD_UPDATED) { body ->
    body.stringType("description", "A probation person record has been updated")
    body.stringMatcher("detailUrl", "^https?://[^/\\s]+/person/probation/$CRN_PATTERN$", personDetailUrl)
    personReference(body, "CRN")
  }

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationRecordMerged(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_RECORD_MERGED) { body ->
    body.stringType("description", "A probation person record has been merged")
    body.stringMatcher("detailUrl", "^https?://[^/\\s]+/person/probation/$CRN_PATTERN$", personDetailUrl)
    personReference(body, "fromCRN")
  }

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationRecordDeleted(builder: PactBuilder): V4Pact = event(builder, CPR_PROBATION_RECORD_DELETED) { body ->
    body.stringType("description", "A probation record has been deleted")
    personReference(body, "CRN")
  }

  @Test
  @PactTestFor(pactMethod = "probationAddressCreated")
  fun `consume probation address created event`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_ADDRESS_CREATED.typeName)
    assertThat(event.detailUrl).isEqualTo(addressDetailUrl)
    assertThat(event.personReference["CRN"]).isEqualTo(crn)
    assertThat(UUID.fromString(event.additionalInformation?.get("cprAddressId").toString())).isEqualTo(cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationAddressUpdated")
  fun `consume probation address updated event`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_ADDRESS_UPDATED.typeName)
    assertThat(event.detailUrl).isEqualTo(addressDetailUrl)
    assertThat(event.personReference["CRN"]).isEqualTo(crn)
    assertThat(UUID.fromString(event.additionalInformation?.get("cprAddressId").toString())).isEqualTo(cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationAddressDeleted")
  fun `consume probation address deleted event without a detail URL`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_ADDRESS_DELETED.typeName)
    assertThat(event.detailUrl).isNull()
    assertThat(event.personReference["CRN"]).isEqualTo(crn)
    assertThat(UUID.fromString(event.additionalInformation?.get("cprAddressId").toString())).isEqualTo(cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationRecordUpdated")
  fun `consume probation record updated event`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_RECORD_UPDATED.typeName)
    assertThat(event.detailUrl).isEqualTo(personDetailUrl)
    assertThat(event.personReference["CRN"]).isEqualTo(crn)
  }

  @Test
  @PactTestFor(pactMethod = "probationRecordMerged")
  fun `consume probation record merged event with the fromCRN required by SAS`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_RECORD_MERGED.typeName)
    assertThat(event.detailUrl).isEqualTo(personDetailUrl)
    assertThat(event.personReference["fromCRN"]).isEqualTo(crn)
  }

  @Test
  @PactTestFor(pactMethod = "probationRecordDeleted")
  fun `consume probation record deleted event`(pact: V4Pact) {
    val event = consume(pact)

    assertThat(event.eventType).isEqualTo(CPR_PROBATION_RECORD_DELETED.typeName)
    assertThat(event.personReference["CRN"]).isEqualTo(crn)
  }

  private fun event(
    builder: PactBuilder,
    eventType: IncomingHmppsDomainEventType,
    content: (LambdaDslObject) -> Unit,
  ): V4Pact = builder
    .usingLegacyMessageDsl()
    .hasPactWith(PROVIDER)
    .given("CPR publishes a ${eventType.typeName} event")
    .expectsToReceive(eventType.typeName)
    .withMetadata(mapOf("contentType" to "application/json"))
    .withContent(
      newJsonBody { body ->
        body.stringValue("eventType", eventType.typeName)
        body.numberValue("version", 1)
        body.stringMatcher(
          "occurredAt",
          "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})$",
          occurredAt,
        )
        content(body)
      }.build(),
    )
    .toPact()

  private fun personReference(body: LambdaDslObject, type: String) {
    body.`object`("personReference") { personReference ->
      personReference.arrayContaining("identifiers") { identifiers ->
        identifiers.`object` { identifier ->
          identifier.stringValue("type", type)
          identifier.stringMatcher("value", "^$CRN_PATTERN$", crn)
        }
      }
    }
  }

  private fun addressInformation(body: LambdaDslObject) {
    body.`object`("additionalInformation") { additionalInformation ->
      additionalInformation.uuid("cprAddressId", cprAddressId)
    }
  }

  private fun consume(pact: V4Pact): SnsDomainEvent {
    val message = requireNotNull(pact.interactions.single().asAsynchronousMessage()).contents.contents.valueAsString()
    val savedEvent = slot<InboxEventEntity>()
    val inboxEventService = mockk<InboxEventService>()
    val sentryService = mockk<SentryService>()
    every { inboxEventService.saveInboxEvent(capture(savedEvent)) } returns Unit
    val listener = HmppsDomainEventListener(jsonMapper, inboxEventService, sentryService)

    // SNS wraps the provider's JSON payload in a Message string before delivering it to SQS.
    listener.processMessage(jsonMapper.writeValueAsString(HmppsDomainEventListener.SQSMessage(message)))

    verify(exactly = 1) { inboxEventService.saveInboxEvent(any()) }
    verify(exactly = 0) { sentryService.captureException(any()) }
    val domainEvent = jsonMapper.readValue(savedEvent.captured.payload, SnsDomainEvent::class.java)
    assertThat(savedEvent.captured.eventType).isEqualTo(domainEvent.eventType)
    assertThat(savedEvent.captured.eventDetailUrl).isEqualTo(domainEvent.detailUrl)
    assertThat(savedEvent.captured.eventOccurredAt).isEqualTo(OffsetDateTime.parse(occurredAt))
    assertThat(savedEvent.captured.processedStatus).isEqualTo(ProcessedStatus.PENDING)
    assertThat(savedEvent.captured.payload).isEqualTo(message)

    assertThat(domainEvent.version).isEqualTo(1)
    assertThat(domainEvent.description).isNotBlank()
    return domainEvent
  }
}
