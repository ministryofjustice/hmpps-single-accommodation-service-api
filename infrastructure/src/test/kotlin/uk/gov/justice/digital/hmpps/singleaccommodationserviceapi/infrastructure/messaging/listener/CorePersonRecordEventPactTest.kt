package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.listener

import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
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
  fun probationAddressCreated(builder: PactBuilder): V4Pact = addressEvent(builder, CPR_PROBATION_ADDRESS_CREATED)

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationAddressUpdated(builder: PactBuilder): V4Pact = addressEvent(builder, CPR_PROBATION_ADDRESS_UPDATED)

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationAddressDeleted(builder: PactBuilder): V4Pact = addressEvent(builder, CPR_PROBATION_ADDRESS_DELETED)

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationRecordUpdated(builder: PactBuilder): V4Pact = recordEvent(builder, CPR_PROBATION_RECORD_UPDATED)

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun probationRecordMerged(builder: PactBuilder): V4Pact = recordEvent(builder, CPR_PROBATION_RECORD_MERGED)

  @Test
  @PactTestFor(pactMethod = "probationAddressCreated")
  fun `consume probation address created event`(pact: V4Pact) {
    assertConsumed(pact, CPR_PROBATION_ADDRESS_CREATED, addressDetailUrl, cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationAddressUpdated")
  fun `consume probation address updated event`(pact: V4Pact) {
    assertConsumed(pact, CPR_PROBATION_ADDRESS_UPDATED, addressDetailUrl, cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationAddressDeleted")
  fun `consume probation address deleted event without a detail URL`(pact: V4Pact) {
    assertConsumed(pact, CPR_PROBATION_ADDRESS_DELETED, null, cprAddressId)
  }

  @Test
  @PactTestFor(pactMethod = "probationRecordUpdated")
  fun `consume probation record updated event`(pact: V4Pact) {
    assertConsumed(pact, CPR_PROBATION_RECORD_UPDATED, personDetailUrl)
  }

  @Test
  @PactTestFor(pactMethod = "probationRecordMerged")
  fun `consume probation record merged event with the CRN required by SAS`(pact: V4Pact) {
    assertConsumed(pact, CPR_PROBATION_RECORD_MERGED, personDetailUrl)
  }

  private fun addressEvent(builder: PactBuilder, eventType: IncomingHmppsDomainEventType): V4Pact = event(
    builder,
    eventType,
    address = true,
    detailUrl = addressDetailUrl.takeUnless { eventType == CPR_PROBATION_ADDRESS_DELETED },
  )

  private fun recordEvent(builder: PactBuilder, eventType: IncomingHmppsDomainEventType): V4Pact = event(
    builder,
    eventType,
    address = false,
    detailUrl = personDetailUrl,
  )

  private fun event(
    builder: PactBuilder,
    eventType: IncomingHmppsDomainEventType,
    address: Boolean,
    detailUrl: String?,
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
        val description = when (eventType) {
          CPR_PROBATION_ADDRESS_CREATED -> "A probation address has been created for a person"
          CPR_PROBATION_ADDRESS_UPDATED -> "A probation address has been updated for a person"
          CPR_PROBATION_ADDRESS_DELETED -> "A probation address has been deleted for a person"
          CPR_PROBATION_RECORD_UPDATED -> "A probation person record has been updated"
          CPR_PROBATION_RECORD_MERGED -> "A probation person record has been merged"
          else -> error("Unsupported CPR event type: $eventType")
        }
        body.stringType("description", description)
        body.stringMatcher(
          "occurredAt",
          "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})$",
          occurredAt,
        )
        if (detailUrl != null) {
          val path = "/person/probation/$CRN_PATTERN" + if (address) "/address/$UUID_PATTERN" else ""
          body.stringMatcher("detailUrl", "^https?://[^/\\s]+$path$", detailUrl)
        }
        body.`object`("personReference") { personReference ->
          personReference.arrayContaining("identifiers") { identifiers ->
            identifiers.`object` { identifier ->
              identifier.stringValue("type", "CRN")
              identifier.stringMatcher("value", "^$CRN_PATTERN$", crn)
            }
          }
        }
        if (address) {
          body.`object`("additionalInformation") { additionalInformation ->
            additionalInformation.uuid("cprAddressId", cprAddressId)
          }
        }
      }.build(),
    )
    .toPact()

  private fun assertConsumed(
    pact: V4Pact,
    eventType: IncomingHmppsDomainEventType,
    detailUrl: String?,
    expectedAddressId: UUID? = null,
  ) {
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
    assertThat(savedEvent.captured.eventType).isEqualTo(eventType.typeName)
    assertThat(savedEvent.captured.eventDetailUrl).isEqualTo(detailUrl)
    assertThat(savedEvent.captured.eventOccurredAt).isEqualTo(OffsetDateTime.parse(occurredAt))
    assertThat(savedEvent.captured.processedStatus).isEqualTo(ProcessedStatus.PENDING)
    assertThat(savedEvent.captured.payload).isEqualTo(message)

    val domainEvent = jsonMapper.readValue(savedEvent.captured.payload, SnsDomainEvent::class.java)
    assertThat(domainEvent.version).isEqualTo(1)
    assertThat(domainEvent.description).isNotBlank()
    assertThat(domainEvent.personReference.findCrn()).isEqualTo(crn)
    if (expectedAddressId != null) {
      val addressId = requireNotNull(domainEvent.additionalInformation?.get("cprAddressId")).toString()
      assertThat(UUID.fromString(addressId)).isEqualTo(expectedAddressId)
    }
  }
}
