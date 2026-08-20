package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class IncomingCprProbationAddressCreatedEventIT : IntegrationTestBase() {
  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  private lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @MockitoSpyBean
  lateinit var caseRefreshRequestService: CaseRefreshRequestService

  lateinit var crn: String
  private val eventType = "core-person-record.probation.address.created"
  private val eventDescription = "A probation address has been created for a person"

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

  @ParameterizedTest
  @EnumSource(value = AddressStatusCode::class, names = ["PR", "PR1"])
  fun `should create a new proposed accommodation record when CRN matches a case, address status is PR and SAS does NOT already have a matching record`(
    proposedAddressStatusCode: AddressStatusCode,
  ) {
    val newlyCreatedAddressInCpr = buildCanonicalAddress(
      cprAddressId = cprAddressId,
      noFixedAbode = false,
      typeVerified = true,
      startDate = LocalDate.now(ZoneOffset.UTC),
      endDate = null,
      postcode = "New postcode",
      subBuildingName = "New subBuildingName",
      buildingName = "New buildingName",
      buildingNumber = "New buildingNumber",
      thoroughfareName = "New thoroughfareName",
      dependentLocality = "New dependentLocality",
      postTown = "New postTown",
      county = "New county",
      country = "New country",
      uprn = "New uprn",
      status = CanonicalAddressStatus(
        code = proposedAddressStatusCode.name,
        description = proposedAddressStatusCode.description,
      ),
      usages = listOf(
        CanonicalAddressUsage(
          usageCode = CanonicalAddressUsageCode(
            code = AddressUsageCode.A07B.name,
            description = AddressUsageCode.A07B.description,
          ),
          isActive = true,
        ),
      ),
    )
    CorePersonRecordStubs.getProbationAddressOKResponse(
      crn = crn,
      cprAddressId = cprAddressId,
      response = newlyCreatedAddressInCpr,
    )

    publishCprProbationAddressCreatedEvent(
      cprAddressId = cprAddressId,
    )

    waitForEntity {
      proposedAccommodationRepository.findByCprAddressId(cprAddressId)
    }

    val newlyCreatedProposedAccommodation = proposedAccommodationRepository.findByCprAddressId(cprAddressId)
    assertThat(newlyCreatedProposedAccommodation).isNotNull
    assertThat(newlyCreatedProposedAccommodation?.caseId).isEqualTo(caseEntity.id)
    assertThat(newlyCreatedProposedAccommodation?.cprAddressId?.toString()).isEqualTo(cprAddressId.toString())
    assertThat(newlyCreatedProposedAccommodation?.accommodationTypeId).isEqualTo(
      accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07B.name)!!.id,
    )
    assertThat(newlyCreatedProposedAccommodation?.accommodationStatusId).isEqualTo(
      accommodationStatusRepository.findByCodeAndActiveIsTrue(proposedAddressStatusCode.name)!!.id,
    )
    assertThat(newlyCreatedProposedAccommodation?.startDate).isEqualTo(newlyCreatedAddressInCpr.startDate?.let(LocalDate::parse))
    assertThat(newlyCreatedProposedAccommodation?.endDate).isNull()
    assertThat(newlyCreatedProposedAccommodation?.postcode).isEqualTo(newlyCreatedAddressInCpr.postcode)
    assertThat(newlyCreatedProposedAccommodation?.subBuildingName).isEqualTo(newlyCreatedAddressInCpr.subBuildingName)
    assertThat(newlyCreatedProposedAccommodation?.buildingName).isEqualTo(newlyCreatedAddressInCpr.buildingName)
    assertThat(newlyCreatedProposedAccommodation?.buildingNumber).isEqualTo(newlyCreatedAddressInCpr.buildingNumber)
    assertThat(newlyCreatedProposedAccommodation?.thoroughfareName).isEqualTo(newlyCreatedAddressInCpr.thoroughfareName)
    assertThat(newlyCreatedProposedAccommodation?.dependentLocality).isEqualTo(newlyCreatedAddressInCpr.dependentLocality)
    assertThat(newlyCreatedProposedAccommodation?.postTown).isEqualTo(newlyCreatedAddressInCpr.postTown)
    assertThat(newlyCreatedProposedAccommodation?.county).isEqualTo(newlyCreatedAddressInCpr.county)
    assertThat(newlyCreatedProposedAccommodation?.country).isNull()
    assertThat(newlyCreatedProposedAccommodation?.uprn).isEqualTo(newlyCreatedAddressInCpr.uprn)
    assertThat(newlyCreatedProposedAccommodation?.accommodationSource).isEqualTo(AccommodationSource.DELIUS)
    assertThat(newlyCreatedProposedAccommodation?.noFixedAbode).isEqualTo(newlyCreatedAddressInCpr.noFixedAbode)
    assertThat(newlyCreatedProposedAccommodation?.typeVerified).isEqualTo(newlyCreatedAddressInCpr.typeVerified)
    assertThat(newlyCreatedProposedAccommodation?.deleted).isFalse()
    // The exact auditor recorded for a brand new insert can resolve to either the DELIUS system user (set explicitly
    // around the save) or the SAS system user (the security context under which the inbox event is processed),
    // depending on Hibernate flush timing - so we only assert it is one of these two expected system users.
    assertThat(newlyCreatedProposedAccommodation?.createdByUserId).isIn(userIdOfDeliusSyncUser, userIdOfSasSystemUser)
    assertThat(newlyCreatedProposedAccommodation?.lastUpdatedByUserId).isIn(userIdOfDeliusSyncUser, userIdOfSasSystemUser)

    testInboxEventHelper.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(cprAddressId),
      processedStatus = ProcessedStatus.PROCESSED,
    )
    verify(caseRefreshRequestService).requestLiveRefresh(caseEntity.id)
  }

  @Test
  fun `should update the existing matching record for idempotency when SAS already has a record for the created address`() {
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

    publishCprProbationAddressCreatedEvent(
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

    testInboxEventHelper.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(cprAddressId),
      processedStatus = ProcessedStatus.PROCESSED,
    )

    verify(caseRefreshRequestService).requestLiveRefresh(caseEntity.id)
  }

  @Test
  fun `should ignore incoming HMPPS CPR_PROBATION_ADDRESS_CREATED domain event when the CRN does NOT match a SAS case`() {
    val unmatchedCrn = UUID.randomUUID().toString()

    publishCprProbationAddressCreatedEvent(
      cprAddressId = cprAddressId,
      crnOverride = unmatchedCrn,
    )

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)

    assertThat(proposedAccommodationRepository.findByCprAddressId(cprAddressId)).isNull()

    testInboxEventHelper.assertInboxEvent(
      crn = unmatchedCrn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(cprAddressId, unmatchedCrn),
      processedStatus = ProcessedStatus.IGNORED,
    )
    verify(caseRefreshRequestService, times(0)).requestLiveRefresh(caseEntity.id)
  }

  @Test
  fun `should ignore incoming HMPPS CPR_PROBATION_ADDRESS_CREATED domain event when the address status is NOT PR or PR1 and SAS does NOT have a matching record`() {
    val mainAddressInCpr = buildCanonicalAddress(
      cprAddressId = cprAddressId,
      status = CanonicalAddressStatus(
        code = AddressStatusCode.M.name,
        description = AddressStatusCode.M.description,
      ),
    )
    CorePersonRecordStubs.getProbationAddressOKResponse(
      crn = crn,
      cprAddressId = cprAddressId,
      response = mainAddressInCpr,
    )

    publishCprProbationAddressCreatedEvent(
      cprAddressId = cprAddressId,
    )

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)

    assertThat(proposedAccommodationRepository.findByCprAddressId(cprAddressId)).isNull()

    testInboxEventHelper.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(cprAddressId),
      processedStatus = ProcessedStatus.IGNORED,
    )

    verify(caseRefreshRequestService, times(1)).requestLiveRefresh(caseEntity.id)
  }

  private fun eventDetailUrl(cprAddressId: UUID, eventCrn: String = crn) = "${sasWiremock.baseUrl()}/person/probation/$eventCrn/address/$cprAddressId"

  private fun publishCprProbationAddressCreatedEvent(cprAddressId: UUID, crnOverride: String? = null) {
    val eventCrn = crnOverride ?: crn
    val snsEvent = """ 
      {
       "eventType":"$eventType",
       "version":1,
       "occurredAt":"2026-07-24T13:58:28.076572456+01:00",
       "description":"$eventDescription",
       "detailUrl":"${eventDetailUrl(cprAddressId, eventCrn)}",
       "personReference":{
          "identifiers":[
             {
                "type":"CRN",
                "value":"$eventCrn"
             }
          ]
       },
       "additionalInformation":{
          "cprAddressId":"$cprAddressId",
          "deliusAddressId":null
       }
      }
    """.trimIndent()

    testInboxEventHelper.publish(snsEvent, eventType)
  }
}
