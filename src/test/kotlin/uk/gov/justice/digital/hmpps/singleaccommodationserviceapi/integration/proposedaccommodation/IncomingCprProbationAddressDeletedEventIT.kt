package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestPropertySource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class IncomingCprProbationAddressDeletedEventIT : IntegrationTestBase() {
  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  lateinit var crn: String
  private val eventType = "core-person-record.probation.address.deleted"
  private val eventDescription = "A probation address has been deleted for a person"

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
    cacheHelper.cacheValueByCrn(crn, cacheKey = GET_CORE_PERSON_RECORD_BY_CRN, cacheValue = buildCorePersonRecord())
  }

  @Test
  fun `should process incoming HMPPS CPR_PROBATION_ADDRESS_DELETED domain event and soft-delete related record when SAS has a matching record`() {
    val preExistingProposedAccommodation = buildProposedAccommodationEntity(
      cprAddressId = cprAddressId,
      caseId = caseEntity.id,
      accommodationSource = AccommodationSource.SAS,
      name = null,
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    proposedAccommodationRepository.save(preExistingProposedAccommodation)

    publishCprProbationAddressDeletedEvent(
      cprAddressId = cprAddressId,
    )

    waitForEntity {
      proposedAccommodationRepository.findByIdAndDeleted(
        id = preExistingProposedAccommodation.id,
        deleted = true,
      )
    }

    val latestProposedAccommodation = proposedAccommodationRepository.findByIdOrNull(preExistingProposedAccommodation.id)
    assertThat(latestProposedAccommodation?.id).isEqualTo(preExistingProposedAccommodation.id)
    assertThat(latestProposedAccommodation?.deleted).isTrue()
    assertThat(latestProposedAccommodation?.createdByUserId).isEqualTo(userIdOfTestDataSetupUser)
    assertThat(latestProposedAccommodation?.lastUpdatedByUserId).isEqualTo(userIdOfSasSystemUser)

    inboxEventHelper.assertMessageProcessed()
    cacheHelper.assertCacheEntryEvicted(crn, GET_CORE_PERSON_RECORD_BY_CRN)
  }

  @Test
  fun `should ignore incoming HMPPS CPR_PROBATION_ADDRESS_DELETED domain event when SAS does NOT have a matching record`() {
    val preExistingProposedAccommodation = buildProposedAccommodationEntity(
      cprAddressId = cprAddressId,
      caseId = caseEntity.id,
      accommodationSource = AccommodationSource.SAS,
      name = null,
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    proposedAccommodationRepository.save(preExistingProposedAccommodation)

    val unmatchingCprAddressId = UUID.randomUUID()
    publishCprProbationAddressDeletedEvent(cprAddressId = unmatchingCprAddressId)

    inboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)

    val latestProposedAccommodation =
      proposedAccommodationRepository.findByIdOrNull(preExistingProposedAccommodation.id)!!
    assertThat(latestProposedAccommodation.id).isEqualTo(preExistingProposedAccommodation.id)
    assertThat(latestProposedAccommodation.deleted).isFalse()
    assertThat(latestProposedAccommodation.createdByUserId).isEqualTo(userIdOfTestDataSetupUser)
    assertThat(latestProposedAccommodation.lastUpdatedByUserId).isEqualTo(userIdOfTestDataSetupUser)

    cacheHelper.assertCacheEntryExists(crn, GET_CORE_PERSON_RECORD_BY_CRN)
  }

  private fun publishCprProbationAddressDeletedEvent(cprAddressId: UUID) {
    val snsEvent = """ 
      {
        "eventType": "$eventType",
        "version":1,
        "description": "$eventDescription",
        "additionalInformation": {
          "cprAddressId": "$cprAddressId",
          "deliusAddressId": null
        },
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
