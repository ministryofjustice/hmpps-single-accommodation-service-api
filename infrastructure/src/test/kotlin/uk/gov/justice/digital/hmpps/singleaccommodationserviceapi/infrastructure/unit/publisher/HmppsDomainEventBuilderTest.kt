package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.HmppsDomainEventUrlConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationDeletedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationPersonArrivedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_DELETED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_PERSON_ARRIVED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.publisher.HmppsDomainEventBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class HmppsDomainEventBuilderTest {
  private val urlConfig = HmppsDomainEventUrlConfig(
    proposedAccommodationUpdatedEventDetailsUrl = "http://sas/proposed-accommodation/#id",
    dutyToReferEventDetailsUrl = "http://sas/duty-to-refer/#id",
  )
  private val objectMapper = mockk<ObjectMapper>()
  private val builder = HmppsDomainEventBuilder(urlConfig, objectMapper)

  private val aggregateId = UUID.randomUUID()
  private val cprAddressId = UUID.randomUUID()
  private val createdAt = Instant.parse("2026-01-02T03:04:05Z")

  private fun outboxEvent(type: SingleAccommodationServiceDomainEventType) = OutboxEventEntity(
    id = UUID.randomUUID(),
    aggregateId = aggregateId,
    aggregateType = "Aggregate",
    domainEventType = type.name,
    payload = "{}",
    createdAt = createdAt,
    processedStatus = ProcessedStatus.PENDING,
    processedAt = null,
  )

  @Test
  fun `accommodation updated includes detail url and cpr address id`() {
    val entity = outboxEvent(SAS_ACCOMMODATION_UPDATED)
    every { objectMapper.readValue(entity.payload, AccommodationUpdatedDomainEvent::class.java) } returns
      AccommodationUpdatedDomainEvent(aggregateId, cprAddressId)

    val result = builder.build(entity, SAS_ACCOMMODATION_UPDATED)

    assertThat(result.eventType).isEqualTo("sas.accommodation.updated")
    assertThat(result.description).isEqualTo("Accommodation has been updated")
    assertThat(result.version).isEqualTo(1)
    assertThat(result.detailUrl).isEqualTo("http://sas/proposed-accommodation/$aggregateId")
    assertThat(result.occurredAt).isEqualTo(createdAt.atOffset(ZoneOffset.UTC))
    assertThat(result.additionalInformation).isEqualTo(mapOf("corePersonAddressId" to cprAddressId.toString()))
  }

  @Test
  fun `accommodation deleted has no detail url but includes cpr address id`() {
    val entity = outboxEvent(SAS_ACCOMMODATION_DELETED)
    every { objectMapper.readValue(entity.payload, AccommodationDeletedDomainEvent::class.java) } returns
      AccommodationDeletedDomainEvent(aggregateId, cprAddressId)

    val result = builder.build(entity, SAS_ACCOMMODATION_DELETED)

    assertThat(result.eventType).isEqualTo("sas.accommodation.deleted")
    assertThat(result.detailUrl).isNull()
    assertThat(result.additionalInformation).isEqualTo(mapOf("corePersonAddressId" to cprAddressId.toString()))
  }

  @Test
  fun `person arrived includes detail url and cpr address id`() {
    val entity = outboxEvent(SAS_ACCOMMODATION_PERSON_ARRIVED)
    every { objectMapper.readValue(entity.payload, AccommodationPersonArrivedDomainEvent::class.java) } returns
      AccommodationPersonArrivedDomainEvent(aggregateId, cprAddressId)

    val result = builder.build(entity, SAS_ACCOMMODATION_PERSON_ARRIVED)

    assertThat(result.eventType).isEqualTo("sas.accommodation.person.arrived")
    assertThat(result.detailUrl).isEqualTo("http://sas/proposed-accommodation/$aggregateId")
    assertThat(result.additionalInformation).isEqualTo(mapOf("corePersonAddressId" to cprAddressId.toString()))
  }

  @ParameterizedTest
  @EnumSource(value = SingleAccommodationServiceDomainEventType::class, names = ["SAS_DUTY_TO_REFER_UPDATED", "SAS_DUTY_TO_REFER_CREATED"])
  fun `duty to refer events have duty to refer url and empty additional information`(type: SingleAccommodationServiceDomainEventType) {
    val result = builder.build(outboxEvent(type), type)

    assertThat(result.eventType).isEqualTo(type.typeName)
    assertThat(result.description).isEqualTo(type.typeDescription)
    assertThat(result.detailUrl).isEqualTo("http://sas/duty-to-refer/$aggregateId")
    assertThat(result.additionalInformation).isEmpty()
  }
}
