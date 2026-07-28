package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class IncomingCprProbationAddressUpdatedEventIT : IntegrationTestBase() {
  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  private lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic") ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  lateinit var crn: String
  private val eventType = "core-person-record.probation.address.updated"
  private val eventDescription = "A probation address has been updated for a person"

  private val cprAddressId = UUID.randomUUID()
  private lateinit var caseEntity: CaseEntity

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
    createDeliusSyncUser()
    createSasSystemUser()
    databaseUtils.truncate(
      DatabaseUtils.SasTables.SAS_CASE,
      DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION,
      DatabaseUtils.SasTables.OUTBOX_EVENT,
      DatabaseUtils.SasTables.INBOX_EVENT,
    )

    crn = UUID.randomUUID().toString()
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
  }

  @Test
  fun `should process incoming HMPPS CPR_PROBATION_ADDRESS_UPDATED domain event and update related record when SAS has a matching record`() {
    val startDate = LocalDate.now(ZoneOffset.UTC).minusDays(20)
    val sasOriginProposedAccommodationEntity = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      cprAddressId = cprAddressId,
      accommodationSource = AccommodationSource.SAS,
      name = null,
      noFixedAbode = false,
      typeVerified = false,
      startDate = startDate,
      endDate = null,
      postcode = "Original postcode",
      subBuildingName = "Original subBuildingName",
      buildingName = "Original buildingName",
      buildingNumber = "Original buildingNumber",
      thoroughfareName = "Original thoroughfareName",
      dependentLocality = "Original dependentLocality",
      postTown = "Original postTown",
      county = "Original county",
      country = "Original country",
      uprn = "Original uprn",
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07B.name)!!,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    proposedAccommodationRepository.save(sasOriginProposedAccommodationEntity)

    val updatedStartDate = startDate.minusDays(10)
    val updatedEndDate = updatedStartDate.plusDays(22)
    val equivalentRecordInCprWithUpdatesOnAllFields = buildCanonicalAddress(
      cprAddressId = cprAddressId,
      noFixedAbode = true,
      typeVerified = true,
      startDate = updatedStartDate,
      endDate = updatedEndDate,
      postcode = "Updated postcode",
      subBuildingName = "Updated subBuildingName",
      buildingName = "Updated buildingName",
      buildingNumber = "Updated buildingNumber",
      thoroughfareName = "Updated thoroughfareName",
      dependentLocality = "Updated dependentLocality",
      postTown = "Updated postTown",
      county = "Updated county",
      country = "Updated country",
      uprn = "Updated uprn",
      status = CanonicalAddressStatus(
        code = AddressStatusCode.PR1.name,
        description = AddressStatusCode.PR1.description,
      ),
      usages = listOf(
        CanonicalAddressUsage(
          usageCode = CanonicalAddressUsageCode(
            code = AddressUsageCode.A07A.name,
            description = AddressUsageCode.A07A.description,
          ),
          isActive = true,
        ),
      ),
    )
    CorePersonRecordStubs.getProbationAddressOKResponse(
      crn = crn,
      cprAddressId = cprAddressId,
      response = equivalentRecordInCprWithUpdatesOnAllFields,
    )

    publishCprProbationAddressUpdatedEvent(
      cprAddressId = cprAddressId,
    )

    val newAccommodationStatus = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR1.name)!!

    waitForEntity {
      proposedAccommodationRepository.findByIdAndAccommodationStatusId(
        id = sasOriginProposedAccommodationEntity.id,
        accommodationStatusId = newAccommodationStatus.id,
      )
    }

    val latestProposedAccommodation = proposedAccommodationRepository.findByIdOrNull(sasOriginProposedAccommodationEntity.id)
    assertThat(latestProposedAccommodation).isNotNull
    assertThat(latestProposedAccommodation?.id).isEqualTo(sasOriginProposedAccommodationEntity.id)
    assertThat(latestProposedAccommodation?.caseId).isEqualTo(caseEntity.id)
    assertThat(latestProposedAccommodation?.cprAddressId?.toString()).isEqualTo(cprAddressId.toString())
    assertThat(latestProposedAccommodation?.accommodationTypeId).isEqualTo(
      accommodationTypeRepository.findByCodeAndActiveIsTrue(
        equivalentRecordInCprWithUpdatesOnAllFields.usages.first().usageCode.code!!,
      )!!.id,
    )
    assertThat(latestProposedAccommodation?.accommodationStatusId).isEqualTo(newAccommodationStatus.id)
    assertThat(latestProposedAccommodation?.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(latestProposedAccommodation?.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(latestProposedAccommodation?.startDate).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.startDate?.let(LocalDate::parse))
    assertThat(latestProposedAccommodation?.endDate).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.endDate?.let(LocalDate::parse))
    assertThat(latestProposedAccommodation?.postcode).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.postcode)
    assertThat(latestProposedAccommodation?.subBuildingName).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.subBuildingName)
    assertThat(latestProposedAccommodation?.buildingName).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.buildingName)
    assertThat(latestProposedAccommodation?.buildingNumber).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.buildingNumber)
    assertThat(latestProposedAccommodation?.thoroughfareName).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.thoroughfareName)
    assertThat(latestProposedAccommodation?.dependentLocality).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.dependentLocality)
    assertThat(latestProposedAccommodation?.postTown).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.postTown)
    assertThat(latestProposedAccommodation?.county).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.county)
    assertThat(latestProposedAccommodation?.country).isNull()
    assertThat(latestProposedAccommodation?.uprn).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.uprn)
    assertThat(latestProposedAccommodation?.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(latestProposedAccommodation?.noFixedAbode).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.noFixedAbode)
    assertThat(latestProposedAccommodation?.typeVerified).isEqualTo(equivalentRecordInCprWithUpdatesOnAllFields.typeVerified)
    assertThat(latestProposedAccommodation?.deleted).isFalse()
    assertThat(latestProposedAccommodation?.createdByUserId).isEqualTo(userIdOfTestDataSetupUser)
    assertThat(latestProposedAccommodation?.lastUpdatedByUserId).isEqualTo(userIdOfSasSystemUser)

    assertThatSingleInboxEventIsAsExpected(
      processedStatus = ProcessedStatus.PROCESSED,
    )
  }

  @Test
  fun `should ignore incoming HMPPS CPR_PROBATION_ADDRESS_UPDATED domain event when SAS does NOT have a matching record`() {
    val preExistingProposedAccommodation = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      cprAddressId = cprAddressId,
      accommodationSource = AccommodationSource.SAS,
      name = null,
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07B.name)!!,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    proposedAccommodationRepository.save(preExistingProposedAccommodation)

    val unmatchingCprAddressId = UUID.randomUUID()
    publishCprProbationAddressUpdatedEvent(
      cprAddressId = unmatchingCprAddressId,
    )

    waitForEntity {
      inboxEventRepository.findAllByProcessedStatus(
        processedStatus = ProcessedStatus.IGNORED,
        pageable = PageRequest.of(0, 10),
      ).firstOrNull()
    }

    val latestProposedAccommodation = proposedAccommodationRepository.findByIdOrNull(preExistingProposedAccommodation.id)
    assertThat(latestProposedAccommodation?.createdByUserId).isEqualTo(userIdOfTestDataSetupUser)
    assertThat(latestProposedAccommodation?.lastUpdatedByUserId).isEqualTo(userIdOfTestDataSetupUser)

    assertThatSingleInboxEventIsAsExpected(
      processedStatus = ProcessedStatus.IGNORED,
    )
  }

  private fun publishCprProbationAddressUpdatedEvent(cprAddressId: UUID) {
    val snsEvent = """ 
      {
       "eventType":"$eventType",
       "version":1,
       "occurredAt":"2026-07-24T13:58:28.076572456+01:00",
       "description":"$eventDescription",
       "detailUrl":"${sasWiremock.baseUrl()}/person/probation/$crn/address/$cprAddressId",
       "personReference":{
          "identifiers":[
             {
                "type":"CRN",
                "value":"$crn"
             }
          ]
       },
       "additionalInformation":{
          "cprAddressId":"$cprAddressId",
          "deliusAddressId":null
       }
      }
    """.trimIndent()

    domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(snsEvent)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        ).build(),
    )
  }

  private fun assertThatSingleInboxEventIsAsExpected(processedStatus: ProcessedStatus) {
    val inboxEvents = inboxEventRepository.findAll()
    Assertions.assertThat(inboxEvents).hasSize(1)
    val inboxEvent = inboxEvents.first()
    val cprProbationAddressDeletedEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    Assertions.assertThat(cprProbationAddressDeletedEvent.personReference.findCrn()).isEqualTo(crn)
    Assertions.assertThat(inboxEvent.eventType).isEqualTo(eventType)
    Assertions.assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
