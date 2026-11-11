package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.util.SasSarFixtureAsserter
import java.time.LocalDate

/**
 * For general information on SARs and testing, see docs/subject_access_requests.md
 *
 * These tests will fail when the template or test data changes. To regenerate the expected outputs
 * run `./gradlew updateSarFixtures` and validate the updated files
 */
class SarComplianceTest : SarTestBase() {

  @Autowired
  lateinit var sarTestReportGenerator: SarTestReportGenerator

  private val testFromDate: LocalDate = LocalDate.of(2019, 1, 1)
  private val testToDate: LocalDate = LocalDate.of(2026, 12, 31)

  private val expectedApiResponsePath = "/sar/sas-expected-api-response.json"
  private val expectedReportPath = "/sar/sas-expected-report.html"
  private val generatedApiResponseFilename = "sas-expected-api-response.json.log"
  private val generatedReportFilename = "sas-expected-report.html.log"

  private val localAuthorityArea: LocalAuthorityAreaEntity by lazy {
    localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
  }

  private val asserter by lazy {
    SasSarFixtureAsserter(
      sasSarHelper = sasSarIntegrationTestHelper,
      sarHelper = sarIntegrationTestHelper,
      webTestClient = webTestClient,
      expectedApiResponseResourcePath = expectedApiResponsePath,
      expectedReportResourcePath = expectedReportPath,
      generatedApiResponseFilename = generatedApiResponseFilename,
      generatedReportFilename = generatedReportFilename,
    )
  }

  private fun setupTestData() {
    sarTestReportGenerator.setupTestData(this)
  }

  @Test
  fun `SAR Data API should return expected consolidated data for all domains`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = sarTestReportGenerator.testCrn,
      fromDate = testFromDate,
      toDate = testToDate,
      laaId = localAuthorityArea.id,
      laaName = localAuthorityArea.name,
    )
  }

  @Test
  fun `SAR Data API should return expected consolidated data for all domains, no date range`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = sarTestReportGenerator.testCrn,
      fromDate = null,
      toDate = null,
      laaId = localAuthorityArea.id,
      laaName = localAuthorityArea.name,
    )
  }

  @Test
  fun `SAR Template should render as expected`() {
    setupTestData()
    sarIntegrationTestHelper.stubFindUserLastNameWith("Johnson")
    asserter.assertReportMatchesFixture(
      crn = sarTestReportGenerator.testCrn,
      fromDate = testFromDate,
      toDate = testToDate,
      laaName = localAuthorityArea.name,
    )
  }
}
