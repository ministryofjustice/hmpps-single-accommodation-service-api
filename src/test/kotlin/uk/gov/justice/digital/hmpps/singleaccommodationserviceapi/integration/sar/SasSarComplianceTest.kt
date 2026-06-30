package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class SasSarComplianceTest : SasSarTestBase() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  @Autowired
  lateinit var sasSarIntegrationTestHelper: SasSarIntegrationTestHelper

  companion object {
    const val TEST_CRN = "X320741"
    val TEST_CASE_ID = UUID.fromString("936a692e-e1e7-49b1-b44f-1c4497f74fe2")
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

  private fun setupTestData() {
    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    val case = createCase(TEST_CRN, TEST_NOMS_NUMBER, TEST_CASE_ID)

    val laa = localAuthorityAreaRepository.findAll().first()
    val dtr = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = laa.id,
        createdAt = TEST_FROM_DATE.atStartOfDay().toInstant(ZoneOffset.UTC),
        createdByUserId = userIdOfLoggedInDeliusUser,
        lastUpdatedByUserId = userIdOfLoggedInDeliusUser,
      ),
    )

    dtr.notes.add(
      buildDutyToReferNoteEntity(
        dutyToReferEntity = dtr,
        createdByUserId = userIdOfLoggedInDeliusUser,
      ),
    )
    dutyToReferRepository.save(dtr)

    val accType = accommodationTypeRepository.findAll().first()
    val accStatus = accommodationStatusRepository.findAll().first()
    val pa = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        caseId = case.id,
        accommodationTypeEntity = accType,
        accommodationStatusEntity = accStatus,
        createdAt = TEST_FROM_DATE.atStartOfDay().toInstant(ZoneOffset.UTC),
        createdByUserId = userIdOfLoggedInDeliusUser,
        lastUpdatedByUserId = userIdOfLoggedInDeliusUser,
      ),
    )

    pa.notes.add(
      buildProposedAccommodationNoteEntity(
        proposedAccommodationEntity = pa,
        createdByUserId = userIdOfLoggedInDeliusUser,
      ),
    )
    proposedAccommodationRepository.save(pa)
  }

  @Test
  fun `SAS SAR API should return consolidated data for all domains`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = TEST_CRN,
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
