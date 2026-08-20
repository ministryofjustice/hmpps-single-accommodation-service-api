package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CaseAllocatedEventIT : IntegrationTestBase() {
  @Autowired
  lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var outboxEventRepository: OutboxEventRepository

  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String

  private val eventType = IncomingHmppsDomainEventType.PERSON_COMMUNITY_MANAGER_ALLOCATED.typeName
  private val eventDescription = "test event"
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v2/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(SAS_CASE, DUTY_TO_REFER, INBOX_EVENT, OUTBOX_EVENT)
    createSasSystemUser()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when new case`() {
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when case already exists - this is duplicate event scenario to prove idempotency`() {
    caseRepository.save(buildCaseEntity(tierScore = "A3") { withCrn(crn) })
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as FAILED when CPR call fails (as we need CPR identifiers to ensure not creating duplicates)`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A3"))

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    testInboxEventHelper.assertMessageProcessed()

    val case = caseRepository.findByIdentifier(crn, IdentifierType.CRN)
    assertThat(case).isNotNull()
    assertThat(case?.firstName).isNull()
    assertThat(case?.lastName).isNull()
    assertThat(case?.dateOfBirth).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as NOT_PROCESSED when case is NOT allocated to a SAS onboarded team`() {
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
      response = CaseSummaries(
        listOf(
          buildCaseSummary(
            crn = crn,
            manager = buildManager(
              team = buildTeam(
                code = "NOT_ONBOARDED",
              ),
            ),
          ),
        ),
      ),
    )

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
    assertThat(caseRepository.findByIdentifier(crn, IdentifierType.CRN)).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when tier API call fails`() {
    TierStubs.getTierServerErrorResponse(crn)
    val (cpr, cas1CurrentPremises) = stubCaseRefresherUpstreams()

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    assertSuccessful(
      expectedCas1Premises = cas1CurrentPremises,
      expectedTier = null,
      expectedCpr = cpr,
    )
  }

  private fun shouldProcessCaseAllocationEventSuccessfully() {
    val tier = "A3"
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = tier))
    val (cpr, cas1CurrentPremises) = stubCaseRefresherUpstreams()

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    assertSuccessful(
      expectedCas1Premises = cas1CurrentPremises,
      expectedTier = tier,
      expectedCpr = cpr,
    )
  }

  private fun stubCaseRefresherUpstreams(): Pair<CorePersonRecord, Cas1PremisesSummary> {
    val cas1CurrentPremises = buildCas1PremisesSummary(postcode = "SW1A 1AA")
    val cpr = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = cas1CurrentPremises.postcode,
          status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
          usages = listOf(
            CanonicalAddressUsage(
              usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A02.name, description = AddressUsageCode.A02.description),
              isActive = true,
            ),
          ),
        ),
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AD",
          endDate = null,
          status = CanonicalAddressStatus(code = AddressStatusCode.PR.name, description = AddressStatusCode.PR.description),
          usages = listOf(
            CanonicalAddressUsage(
              usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A07A.name, description = AddressUsageCode.A07A.description),
              isActive = true,
            ),
          ),
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, cpr)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
      response = CaseSummaries(listOf(buildCaseSummary(crn = crn, nomsId = "YY09876Y"))),
    )
    ApprovedPremisesStubs.getCas1CurrentPremisesOKResponse(crn, cas1CurrentPremises)
    return cpr to cas1CurrentPremises
  }

  private fun assertSuccessful(
    expectedCas1Premises: Cas1PremisesSummary,
    expectedTier: String?,
    expectedCpr: CorePersonRecord?,
  ) {
    testInboxEventHelper.assertMessageProcessed()

    val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
    assertThat(case.tierScore).isEqualTo(expectedTier)
    assertThat(case.firstName).isEqualTo(expectedCpr?.firstName)
    assertThat(case.lastName).isEqualTo(expectedCpr?.lastName)
    assertThat(case.dateOfBirth).isEqualTo(expectedCpr?.dateOfBirth)
    assertThat(case.accommodationStatus).isEqualTo(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)

    val currentAccommodation = jsonMapper.readValue(case.currentAccommodation, AccommodationSummaryDto::class.java)
    assertThat(currentAccommodation.address.postcode).isEqualTo("SW1A 1AA")
    assertThat(currentAccommodation.type?.code).isEqualTo(AddressUsageCode.A02.name)
    assertThat(currentAccommodation.startDate).isEqualTo(expectedCas1Premises.startDate)
    assertThat(currentAccommodation.endDate).isEqualTo(expectedCas1Premises.endDate)

    val nextAccommodation = jsonMapper.readValue(case.nextAccommodation, AccommodationSummaryDto::class.java)
    assertThat(nextAccommodation.address.postcode).isEqualTo("SW1A 1AD")
    assertThat(nextAccommodation.startDate).isNull()
    assertThat(nextAccommodation.endDate).isNull()
  }

  private fun assertPublishedSNSEvent(
    detailUrl: String,
  ) {
    testSqsDomainEventListener.assertMessageReceived(
      typeName = IncomingHmppsDomainEventType.PERSON_COMMUNITY_MANAGER_ALLOCATED.typeName,
      eventDescription = eventDescription,
      detailUrl = detailUrl,
    )
  }

  private fun publishCaseAllocatedEvent() {
    val snsEvent = """ 
      {
        "eventType": "$eventType",
        "externalId": "$externalId",
        "version": 1,
        "description": "$eventDescription",
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

    testInboxEventHelper.publish(snsEvent, eventType)
  }
}
