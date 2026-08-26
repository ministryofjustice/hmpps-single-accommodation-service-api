package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.PrisonerSearchStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.OffsetDateTime
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CaseRefreshIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  lateinit var crn: String

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(IncomingHmppsDomainEventType::class)
  fun `tests enum for triggering a case refresh`(eventType: IncomingHmppsDomainEventType) {
    val crn = UUID.randomUUID().toString()
    val prisonNumber = UUID.randomUUID().toString()
    val deliusCase = buildCase(crn = crn, nomsNumber = prisonNumber)
    val cprAddressId = UUID.randomUUID()
    val expectedTierValue = UUID.randomUUID().toString()

    caseRepository.save(buildCaseEntity { withCrn(crn) })

    var domainEvent = SnsDomainEvent(
      eventType = eventType.typeName,
      version = 1,
      description = eventType.name,
      detailUrl = "test",
      occurredAt = OffsetDateTime.now(),
      personReference = PersonReference(listOf(PersonIdentifier(type = "CRN", value = crn))),
      additionalInformation = null,
    )
    CorePersonRecordStubs.getProbationAddressOKResponse(
      crn = crn,
      response = buildCanonicalAddress(),
      cprAddressId = cprAddressId,
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = buildCorePersonRecord())
    PrisonerSearchStubs.getPrisonerOKResponse(
      prisonNumber = prisonNumber,
      response = buildPrisoner(prisonNumber = prisonNumber),
    )
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
      response = CaseSummaries(listOf(buildCaseSummary(crn = crn, nomsId = prisonNumber))),
    )

    ProbationIntegrationDeliusStubs.getCaseByCrn(crn = crn, response = deliusCase)

    TierStubs.getTierOKResponse(crn = crn, response = buildTier(tierScore = expectedTierValue))
    ApprovedPremisesStubs.getCas1CurrentPremisesOKResponse(crn = crn, response = buildCas1PremisesSummary())
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = buildCas1Application())
    ApprovedPremisesStubs.getCas3CurrentPremisesOKResponse(crn = crn, response = buildCas3PremisesSummary())
    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = buildCas3Application())

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
          domainEvent.copy(detailUrl = detailUrl, additionalInformation = mapOf("cprAddressId" to cprAddressId))
        true
      }

      IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED -> true

      IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED -> true

      IncomingHmppsDomainEventType.PERSON_COMMUNITY_MANAGER_ALLOCATED -> false
    }

    testInboxEventHelper.publish(domainEvent)

    if (shouldRefresh) {
      waitFor {
        assertThat(caseRefreshRequestRepository.findAll()).hasSize(1)
      }
    } else {
      waitFor { assertThat(caseRefreshRequestRepository.findAll()).hasSize(0) }
    }

    // Hard-fail if any outbound request was not stubbed for this scenario.
    waitFor {
      val matchingEvents = inboxEventRepository.findAll().filter { it.payload.contains(crn) }
      assertThat(matchingEvents).isNotEmpty
      assertThat(matchingEvents.none { it.processedStatus == ProcessedStatus.PENDING }).isTrue()
      assertThat(sasWiremock.findUnmatchedRequests().requests.map { it.url }).isEmpty()
    }
  }
}
