package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.DomainEventIntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WiremockStubber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierEventHandlerConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(
  properties = [
    "scheduling.enabled=true",
  ],
)
class CaseProjectionRefreshIT : DomainEventIntegrationTestBase() {

  @Autowired
  lateinit var tierEventHandlerConfig: TierEventHandlerConfig

  private lateinit var crn: String

  private fun tierEventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v3/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    IncomingHmppsDomainEventType::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = ["PROBATION_USER_USERNAME_CHANGED"],
  )
  fun `should refresh case based on event type`(eventType: IncomingHmppsDomainEventType) {
    val crn = UUID.randomUUID().toString()
    val prisonNumber = UUID.randomUUID().toString()

    caseRepository.save(
      buildCaseEntity(tierScore = null) {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      },
    )

    var domainEvent = SnsDomainEvent(
      eventType = eventType.typeName,
      version = 1,
      description = eventType.name,
      detailUrl = "test",
      occurredAt = OffsetDateTime.now(),
      personReference = PersonReference(
        listOf(
          PersonIdentifier(type = "CRN", value = crn),
          PersonIdentifier(type = "NOMS", value = prisonNumber),
        ),
      ),
      additionalInformation = null,
    )

    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn, prisonNumber)

    // identify which event types should trigger a case refresh, add any event specific stubs / data setup.
    val shouldRefresh = when (eventType) {
      IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_CREATED,
      IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_UPDATED,
      IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_DELETED,
      -> {
        val cprAddressId = UUID.randomUUID()
        CorePersonRecordStubs.getProbationAddressOKResponse(
          crn = crn,
          cprAddressId = cprAddressId,
          response = buildCanonicalAddress(cprAddressId = cprAddressId),
        )

        val detailUrl = "${sasWiremock.baseUrl()}/person/probation/$crn/address/$cprAddressId"
        domainEvent =
          domainEvent.copy(
            detailUrl = detailUrl,
            additionalInformation = mapOf("cprAddressId" to cprAddressId),
          )
        true
      }

      IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED,
      IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED,
      IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_RELEASED,
      -> {
        domainEvent =
          domainEvent.copy(
            personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = prisonNumber))),
          )
        true
      }

      IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED,
      IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED,
      IncomingHmppsDomainEventType.APPROVED_PREMISES_BOOKING_CANCELLED,
      IncomingHmppsDomainEventType.APPROVED_PREMISES_BOOKING_CHANGED,
      IncomingHmppsDomainEventType.APPROVED_PREMISES_BOOKING_NOT_ARRIVED,
      IncomingHmppsDomainEventType.APPROVED_PREMISES_BOOKING_MADE,
      IncomingHmppsDomainEventType.ACCOMMODATION_CAS3_BOOKING_CONFIRMED,
      IncomingHmppsDomainEventType.ACCOMMODATION_CAS3_BOOKING_CANCELLED,
      IncomingHmppsDomainEventType.ACCOMMODATION_CAS3_BOOKING_CANCELLED_UPDATED,
      IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_ADDED,
      IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_DELETED,
      IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_DEREGISTERED,
      IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_UPDATED,
      IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED,
      -> true

      IncomingHmppsDomainEventType.PERSON_COMMUNITY_MANAGER_ALLOCATED,
      -> {
        ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
          CaseSummaries(listOf(buildCaseSummary(crn = crn, nomsId = prisonNumber))),
        )
        false
      }

      IncomingHmppsDomainEventType.PROBATION_USER_USERNAME_CHANGED -> throw IllegalArgumentException("This event type is not part of this test.")
    }

    testInboxEventHelper.publish(domainEvent)

    if (shouldRefresh) {
      waitFor {
        // these fields should now be updated on the entity after successful refresh.
        val updated = caseRepository.findByCrn(crn)
        assertThat(updated!!.tierScore).isEqualTo(responses.tier!!.tierScore)
        assertThat(updated.firstName).isEqualTo(responses.cpr!!.firstName)
        assertThat(updated.lastName).isEqualTo(responses.cpr!!.lastName)
      }
    } else {
      waitFor {
        testInboxEventHelper.assertInboxEvent(
          crn,
          eventType = eventType.typeName,
          eventDetailUrl = domainEvent.detailUrl,
          processedStatus = ProcessedStatus.PROCESSED,
        )
        assertThat(caseRefreshRequestRepository.findAll()).hasSize(0)
      }
    }

    waitFor {
      val matchingEvents =
        inboxEventRepository.findAll().filter { it.payload.contains(crn) || it.payload.contains(prisonNumber) }
      assertThat(matchingEvents).hasSize(1)
      assertThat(matchingEvents.none { it.processedStatus == ProcessedStatus.PENDING }).isTrue()
    }

    // Hard-fail if any outbound request was not stubbed for this scenario.
    assertThat(sasWiremock.findUnmatchedRequests().requests.map { it.url }).isEmpty()
  }

  // TODO: Remove this exclusion when CaseAllocationHandler is refactored
  @ParameterizedTest(name = "{0}")
  @EnumSource(
    IncomingHmppsDomainEventType::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = ["PERSON_COMMUNITY_MANAGER_ALLOCATED", "PROBATION_USER_USERNAME_CHANGED"],
  )
  fun `should ignore messages when the case is unknown`(eventType: IncomingHmppsDomainEventType) {
    val crn = UUID.randomUUID().toString()
    val prisonNumber = UUID.randomUUID().toString()

    // this event is generic to pass all event handlers
    val domainEvent = SnsDomainEvent(
      eventType = eventType.typeName,
      version = 1,
      description = eventType.name,
      detailUrl = "test",
      occurredAt = OffsetDateTime.now(),
      personReference = PersonReference(
        listOf(
          PersonIdentifier(type = "CRN", value = crn),
          PersonIdentifier(type = "NOMS", value = prisonNumber),
        ),
      ),
      additionalInformation = mapOf("cprAddressId" to UUID.randomUUID().toString(), "staffCode" to "123456"),
    )

    testInboxEventHelper.publish(domainEvent)

    waitFor {
      testInboxEventHelper.assertInboxEvent(
        crn,
        eventType = eventType.typeName,
        eventDetailUrl = domainEvent.detailUrl,
        processedStatus = ProcessedStatus.IGNORED,
      )
      val matchingEvents =
        inboxEventRepository.findAll().filter { it.payload.contains(crn) || it.payload.contains(prisonNumber) }
      assertThat(matchingEvents).hasSize(1)
      assertThat(matchingEvents.none { it.processedStatus == ProcessedStatus.PENDING }).isTrue()
    }
  }

  @Test
  fun `coalesces duplicate projection change events into one refresh request`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    publishProjectionChangeEvent()
    publishProjectionChangeEvent()

    testInboxEventHelper.assertAllInboxMessagesProcessed(2)
    val refreshRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refreshRequest.generation).isEqualTo(2)
    assertThat(caseRepository.findAll()).hasSize(1)
  }

  private fun publishProjectionChangeEvent() {
    val snsEvent = """
      {
        "eventType": "tier.calculation.changed",
        "externalId": "${UUID.randomUUID()}",
        "version": 1,
        "description": "Tier calculation complete from Tier service",
        "detailUrl": "${tierEventDetailUrl()}",
        "personReference": {
          "identifiers": [
            {
              "type": "CRN",
              "value": "$crn"
            }
          ]
        },
        "occurredAt": "${Instant.now().atOffset(ZoneOffset.UTC)}"
      }
    """.trimIndent()

    testInboxEventHelper.publish(snsEvent, "tier.calculation.changed")
  }
}
