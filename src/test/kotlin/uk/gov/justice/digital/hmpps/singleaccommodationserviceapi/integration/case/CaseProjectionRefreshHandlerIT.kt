package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE_REFRESH_REQUEST
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(
  properties = [
    "scheduling.enabled=true",
    "case-refresh.worker.enabled=false",
  ],
)
class CaseProjectionRefreshHandlerIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  private lateinit var crn: String
  private val eventType = "tier.calculation.changed"
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v3/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    databaseUtils.truncate(SAS_CASE, INBOX_EVENT, SAS_CASE_REFRESH_REQUEST)

    createSasSystemUser()
  }

  @Test
  fun `requests asynchronous Case projection refresh for an existing Case without calling upstream services`() {
    val originalCase = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    publishProjectionChangeEvent()

    inboxEventHelper.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(),
      processedStatus = ProcessedStatus.PROCESSED,
    )

    TierStubs.getTierOKResponse(
      crn = crn,
      response = buildTier(tierScore = "A3"),
    )

    waitFor {
      assertThat(caseRefreshRequestRepository.findAll()).hasSize(1)

      val persistedCase = caseRepository.findByCrn(crn)!!
      assertThat(persistedCase.tierScore).isEqualTo("A1")
      assertThat(persistedCase.cas1ApplicationId).isEqualTo(originalCase.cas1ApplicationId)
      sasWiremock.verify(0, getRequestedFor(urlPathMatching("/v[23]/crn/.*/tier")))
    }
  }

  @Test
  fun `coalesces duplicate projection change events into one refresh request`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    publishProjectionChangeEvent()
    publishProjectionChangeEvent()

    inboxEventHelper.assertAllInboxMessagesProcessed(2)
    val refreshRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refreshRequest.generation).isEqualTo(2)
    assertThat(caseRepository.findAll()).hasSize(1)
  }

  @Test
  fun `ignores a projection change event for an unknown Case`() {
    publishProjectionChangeEvent()

    inboxEventHelper.assertExpectedInboxEvents(
      processedStatus = ProcessedStatus.IGNORED,
      count = 1,
    )

    assertThat(caseRepository.findAll()).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
    sasWiremock.verify(0, getRequestedFor(urlPathMatching("/v[23]/crn/.*/tier")))
  }

  private fun publishProjectionChangeEvent() {
    val snsEvent = """
      {
        "eventType": "$eventType",
        "externalId": "${UUID.randomUUID()}",
        "version": 1,
        "description": "Tier calculation complete from Tier service",
        "detailUrl": "${eventDetailUrl()}",
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

    inboxEventHelper.publish(snsEvent, eventType)
  }
}
