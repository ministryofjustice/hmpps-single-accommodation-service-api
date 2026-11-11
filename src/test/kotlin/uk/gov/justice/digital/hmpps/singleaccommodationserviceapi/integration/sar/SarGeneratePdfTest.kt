package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.util.sarGenerateActual
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SubjectAccessRequestResponse
import java.time.LocalDate

/**
 * For general information on SARs and testing, see docs/subject_access_requests.md
 *
 * These tests will fail when the template or test data changes. To regenerate the expected outputs
 * run `./gradlew updateSarFixtures` and validate the updated files
 */
class SarGeneratePdfTest : SarTestBase() {

  @Autowired
  lateinit var sarTestReportGenerator: SarTestReportGenerator

  private val testFromDate: LocalDate = LocalDate.of(2019, 1, 1)
  private val testToDate: LocalDate = LocalDate.of(2026, 12, 31)

  /**
   * Renders the SAR report as a PDF to build/test-generated/sar-generated-report.pdf for manual review
   * (or run `./gradlew generateSarPdf`), and checks the rendered HTML against sar/sas-expected-render-result.html
   */
  @Test
  fun `SAR report should render PDF as expected`() {
    sarTestReportGenerator.setupTestData(this)

    val dataResponse = sasSarIntegrationTestHelper.requestSarData(
      null,
      sarTestReportGenerator.testCrn,
      testFromDate,
      testToDate,
      webTestClient,
      SubjectAccessRequestResponse::class.java,
    )
    val templateResponse = sasSarIntegrationTestHelper.requestSarTemplate(webTestClient)

    val renderResult = sarIntegrationTestHelper.renderServiceReport(
      data = dataResponse.content,
      templateVersion = "1.0",
      template = templateResponse,
    )

    sarIntegrationTestHelper.renderAndSaveReportAsPdf(renderResult, null, sarTestReportGenerator.testCrn)

    if (sarGenerateActual()) {
      sarIntegrationTestHelper.saveContentToFile(
        renderResult.replace(sarTestReportGenerator.localAuthorityArea.name, "%LAA_NAME%"),
        "sas-expected-render-result.html.log",
      )
      return
    }

    val expected = sarIntegrationTestHelper.getExpectedRenderResult().replace("%LAA_NAME%", sarTestReportGenerator.localAuthorityArea.name)
    sarIntegrationTestHelper.assertHtmlEquals(renderResult, expected)
  }
}
