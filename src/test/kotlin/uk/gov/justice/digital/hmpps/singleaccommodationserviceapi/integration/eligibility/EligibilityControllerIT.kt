package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class EligibilityControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val cas1ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111111")
  private val cas3ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f6-111111111111")
  private val dutyToReferCaseId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111112")

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
      ),
    )
    val corePersonRecordAddresses = buildCorePersonRecordAddresses(
      crn = crn,
      addresses = listOf(
        buildAddress(
          addressStatus = AddressStatus.M,
          endDate = LocalDate.now().plusDays(1),
        ),
      ),
    )
    val tier = buildTier(TierScore.A1)
    val cas1Application = buildCas1Application(id = cas1ApplicationId)
    val cas3Application = buildCas3Application(id = cas3ApplicationId)

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    CorePersonRecordStubs.getCorePersonRecordAddressesOKResponse(crn = crn, response = corePersonRecordAddresses)
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = cas1Application)
    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)
    TierStubs.getTierOKResponse(crn = crn, tier)
  }

  @AfterEach
  fun teardown() {
    dutyToReferRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @Test
  fun `should get eligibility for crn`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val entity = buildCaseEntity(id = dutyToReferCaseId) { withCrn(crn) }
    caseRepository.save(entity)

    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = entity.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetEligibilityResponse(
            crn = crn,
            cas1ApplicationId = cas1ApplicationId,
            cas3ApplicationId = cas3ApplicationId,
            dutyToReferCaseId = dutyToReferCaseId,
            dutyToReferId = existingEntity.id,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            submissionDate = "2026-01-15",
            referenceNumber = "DTR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }
}
