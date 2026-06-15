package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.File

class SasSarFixtureAsserter {
  fun assertJson(actualJson: String, expectedJson: String, fileName: String = "sas-sar-api-response.json") {
    if (System.getenv("SAR_GENERATE_ACTUAL") == "true") {
      File("src/test/resources/$fileName.log").writeText(actualJson)
    }
    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT)
  }

  fun assertHtml(actualHtml: String, expectedHtml: String, fileName: String = "sas-sar-report.html") {
    if (System.getenv("SAR_GENERATE_ACTUAL") == "true") {
      File("src/test/resources/$fileName.log").writeText(actualHtml)
    }
    // Basic whitespace-insensitive comparison for HTML
    assertThat(actualHtml.replace("\\s".toRegex(), ""))
      .isEqualTo(expectedHtml.replace("\\s".toRegex(), ""))
  }
}
