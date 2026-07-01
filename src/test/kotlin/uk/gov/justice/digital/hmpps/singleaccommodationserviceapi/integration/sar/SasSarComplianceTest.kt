package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class SasSarComplianceTest : SasSarTestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  @Autowired
  lateinit var sasSarIntegrationTestHelper: SasSarIntegrationTestHelper

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @Autowired
  lateinit var testClock: MutableTestClock

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var transactionTemplate: TransactionTemplate

  companion object {
    const val TEST_CRN = "X320741"
    val TEST_CASE_ID = UUID.fromString("936a692e-e1e7-49b1-b44f-1c4497f74fe2")
    val TEST_PA_ID = UUID.fromString("3276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_DTR_ID = UUID.fromString("5e0fe44d-6903-42ce-a426-c7b53494ba35")
    var testLaaId = UUID.fromString("8011ec88-52d2-478e-9ee5-4632a0d6a9eb")
    const val TEST_NOMS_NUMBER = "A1234BC"
    val TEST_FROM_DATE: LocalDate = LocalDate.of(2019, 1, 1)
    val TEST_TO_DATE: LocalDate = LocalDate.of(2026, 12, 31)

    const val EXPECTED_API_RESPONSE_PATH = "/sar/sas-expected-api-response.json"
    const val EXPECTED_REPORT_PATH = "/sar/sas-expected-report.html"
    const val GENERATED_API_RESPONSE_FILENAME = "sas-sar-api-response.json.log"
    const val GENERATED_REPORT_FILENAME = "sas-sar-report.html.log"
  }

  private val asserter by lazy {
    SasSarFixtureAsserter(
      sasSarHelper = sasSarIntegrationTestHelper,
      sarHelper = sarIntegrationTestHelper,
      webTestClient = webTestClient,
      expectedApiResponseResourcePath = EXPECTED_API_RESPONSE_PATH,
      expectedReportResourcePath = EXPECTED_REPORT_PATH,
      generatedApiResponseFilename = GENERATED_API_RESPONSE_FILENAME,
      generatedReportFilename = GENERATED_REPORT_FILENAME,
    )
  }

  protected fun setupTestData() {
    transactionTemplate.execute {
      createTestDataSetupUserAndDeliusUser()
      HmppsAuthStubs.stubGrantToken()

      val case = createCase(TEST_CRN, TEST_NOMS_NUMBER, TEST_CASE_ID)

      val laa = localAuthorityAreaRepository.findAll().find { it.name == "Aberdeen City" }
        ?: localAuthorityAreaRepository.save(LocalAuthorityAreaEntity(testLaaId, "Aberdeen City", "ABC", true))
      testLaaId = laa.id

      val dtr = dutyToReferRepository.save(
        buildDutyToReferEntity(
          id = TEST_DTR_ID,
          caseId = case.id,
          localAuthorityAreaId = laa.id,
          createdByUserId = userIdOfTestDataSetupUser,
          lastUpdatedByUserId = userIdOfTestDataSetupUser,
        ),
      )
      dtr.notes.add(buildDutyToReferNoteEntity(dutyToReferEntity = dtr, createdByUserId = userIdOfTestDataSetupUser))
      dutyToReferRepository.save(dtr)

      val accType = accommodationTypeRepository.findAll().find { it.code == "A10" } ?: accommodationTypeRepository.findAll().first()
      val accStatus = accommodationStatusRepository.findAll().first()
      val pa = buildProposedAccommodationEntity(
        id = TEST_PA_ID,
        caseId = case.id,
        accommodationTypeEntity = accType,
        accommodationStatusEntity = accStatus,
        createdByUserId = userIdOfTestDataSetupUser,
        lastUpdatedByUserId = userIdOfTestDataSetupUser,
      )
      pa.notes.add(buildProposedAccommodationNoteEntity(proposedAccommodationEntity = pa, createdByUserId = userIdOfTestDataSetupUser))
      proposedAccommodationRepository.save(pa)

      entityManager.flush()

      // Force timestamps to match fixture
      jdbcTemplate.update(
        "update duty_to_refer set created_at = ?, last_updated_at = ? where id = ?",
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        TEST_DTR_ID,
      )
      jdbcTemplate.update(
        "update proposed_accommodation set created_at = ?, last_updated_at = ? where id = ?",
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        TEST_PA_ID,
      )
      jdbcTemplate.update(
        "update proposed_accommodation_note set created_at = ?, last_updated_at = ? where proposed_accommodation_id = ?",
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
        TEST_PA_ID,
      )
    }
  }

  // This test fails in dev, because we hardcode the laa in the
  @Test
  fun `SAS SAR API should return consolidated data for all domains`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = TEST_CRN,
      laaId = testLaaId,
      fromDate = TEST_FROM_DATE,
      toDate = TEST_TO_DATE,
    )
  }

  @Test
  fun `SAS SAR report should render correctly with all domains`() {
    setupTestData()
    asserter.assertReportMatchesFixture(
      crn = TEST_CRN,
      fromDate = TEST_FROM_DATE,
      toDate = TEST_TO_DATE,
    )
  }
}
