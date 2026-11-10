package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SubjectAccessRequestResponse
import java.time.LocalDate
import java.util.UUID

class SasSarFixtureAsserter(
  private val sasSarHelper: SasSarIntegrationTestHelper,
  private val sarHelper: SarIntegrationTestHelper,
  private val webTestClient: WebTestClient,
  private val expectedApiResponseResourcePath: String,
  private val expectedReportResourcePath: String,
  private val generatedApiResponseFilename: String,
  private val generatedReportFilename: String,
) {

  fun assertApiDataMatchesFixture(
    prn: String? = null,
    crn: String? = null,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
    laaId: UUID,
    laaName: String,
  ) {
    val response = sasSarHelper.requestSarData(prn, crn, fromDate, toDate, webTestClient, SubjectAccessRequestResponse::class.java)
    val actualJson = sarHelper.toJson(response)

    if (generateActual()) {
      sarHelper.saveContentToFile(actualJson, generatedApiResponseFilename)
    } else {
      val expected = sarHelper.getResourceAsString(expectedApiResponseResourcePath)
        .replace("%LAA_ID%", laaId.toString())
        .replace("%LAA_NAME%", laaName)

      assertThatJson(actualJson).`as`("Response content json").isEqualTo(expected)

      assertThat(response.attachments?.isNotEmpty() == true)
        .`as`("Response has attachments")
        .isEqualTo(sarHelper.attachmentsExpected)
    }
  }

  fun assertReportMatchesFixture(
    prn: String? = null,
    crn: String? = null,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
    laaName: String,
  ) {
    val dataResponse = sasSarHelper.requestSarData(prn, crn, fromDate, toDate, webTestClient, SubjectAccessRequestResponse::class.java)
    val templateResponse = sasSarHelper.requestSarTemplate(webTestClient)

    val renderResult = sarHelper.renderServiceReport(
      dataResponse.content,
      "1.0",
      templateResponse,
    )

    if (generateActual()) {
      sarHelper.saveContentToFile(renderResult, generatedReportFilename)
    } else {
      sarHelper.assertHtmlEquals(
        renderResult,
        sarHelper.getResourceAsString(expectedReportResourcePath).replace("%LAA_NAME%", laaName),
      )
    }
  }

  private fun generateActual() = System.getenv("SAR_GENERATE_ACTUAL").toBoolean()
}
