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
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SubjectAccessRequestResponse
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class SasSarPdfReportTest : SasSarTestBase() {

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
    val TEST_PA_ID_2 = UUID.fromString("4276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_CAS1_ID_1 = UUID.fromString("5276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_CAS1_ID_2 = UUID.fromString("6276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_CAS3_ID_1 = UUID.fromString("7276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_CAS3_ID_2 = UUID.fromString("8276ece4-4c1d-4a5f-a50e-ef92c1aee368")
    val TEST_DTR_ID = UUID.fromString("5e0fe44d-6903-42ce-a426-c7b53494ba35")
    val TEST_DTR_ID_2 = UUID.fromString("6e0fe44d-6903-42ce-a426-c7b53494ba35")
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

      val dtrIds = listOf(TEST_DTR_ID, TEST_DTR_ID_2)
      dtrIds.forEachIndexed { index, id ->
        val dtr = dutyToReferRepository.save(
          buildDutyToReferEntity(
            id = id,
            caseId = case.id,
            localAuthorityAreaId = laa.id,
            referenceNumber = "DTR-REF-00${index + 1}",
            createdByUserId = userIdOfTestDataSetupUser,
            lastUpdatedByUserId = userIdOfTestDataSetupUser,
          ),
        )
        dtr.notes.add(buildDutyToReferNoteEntity(dutyToReferEntity = dtr, note = "DTR Note ${getLoremIpsum()} ${index + 1}", createdByUserId = userIdOfTestDataSetupUser))
        dutyToReferRepository.save(dtr)
      }

      val accStatus = accommodationStatusRepository.findAll().first()
      val paConfigs = listOf(
        Triple(TEST_PA_ID, "A10", "Proposed Accommodation 1"),
        Triple(TEST_PA_ID_2, "A11", "Proposed Accommodation 2"),
        Triple(TEST_CAS1_ID_1, "A02", "CAS1 Referral 1"),
        Triple(TEST_CAS1_ID_2, "A02", "CAS1 Referral 2"),
        Triple(TEST_CAS3_ID_1, "A17", "CAS3 Referral 1"),
        Triple(TEST_CAS3_ID_2, "A17", "CAS3 Referral 2"),
      )

      paConfigs.forEachIndexed { index, (id, typeCode, name) ->
        val accType = accommodationTypeRepository.findAll().find { it.code == typeCode } ?: accommodationTypeRepository.findAll().first()
        val pa = buildProposedAccommodationEntity(
          id = id,
          caseId = case.id,
          name = name,
          accommodationTypeEntity = accType,
          accommodationStatusEntity = accStatus,
          createdByUserId = userIdOfTestDataSetupUser,
          lastUpdatedByUserId = userIdOfTestDataSetupUser,
        )
        pa.notes.add(buildProposedAccommodationNoteEntity(proposedAccommodationEntity = pa, note = "PA Note ${getLoremIpsum()} ${index + 1}", createdByUserId = userIdOfTestDataSetupUser))
        proposedAccommodationRepository.save(pa)
      }

      entityManager.flush()

      // Force timestamps to match fixture
      dtrIds.forEach { id ->
        jdbcTemplate.update(
          "update duty_to_refer set created_at = ?, last_updated_at = ? where id = ?",
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          id,
        )
      }

      paConfigs.forEach { (id, _, _) ->
        jdbcTemplate.update(
          "update proposed_accommodation set created_at = ?, last_updated_at = ? where id = ?",
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          id,
        )
        jdbcTemplate.update(
          "update proposed_accommodation_note set created_at = ?, last_updated_at = ? where proposed_accommodation_id = ?",
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          Timestamp.from(Instant.parse("2026-06-30T15:31:04Z")),
          id,
        )
      }
    }
  }

  private fun getLoremIpsum() =
    """
      Lorem ipsum dolor sit amet consectetur adipiscing elit. Elit quisque faucibus ex sapien vitae pellentesque sem. Sem placerat in id cursus mi pretium tellus. 
      Tellus duis convallis tempus leo eu aenean sed. Sed diam urna tempor pulvinar vivamus fringilla lacus. Lacus nec metus bibendum egestas iaculis massa nisl. 
      Nisl malesuada lacinia integer nunc posuere ut hendrerit.
    """.trimIndent()

  @Test
  fun `SAR report should render PDF as expected`() {
    setupTestData()
    sarIntegrationTestHelper.stubFindPrisonNameWith("Moorland (HMP & YOI)")
    sarIntegrationTestHelper.stubFindUserLastNameWith("Johnson")
    sarIntegrationTestHelper.stubFindLocationNameByNomisIdWith("PROPERTY BOX 1")
    sarIntegrationTestHelper.stubFindLocationNameByDpsIdWith("PROPERTY BOX 2")

    val dataResponse = sasSarIntegrationTestHelper.requestSarData(
      null,
      TEST_CRN,
      TEST_FROM_DATE,
      TEST_TO_DATE,
      webTestClient,
      SubjectAccessRequestResponse::class.java,
    )
    val templateResponse = sasSarIntegrationTestHelper.requestSarTemplate(webTestClient)

    val renderResult = sarIntegrationTestHelper.renderServiceReport(
      data = dataResponse.content,
      templateVersion = "1.0",
      template = templateResponse,
    )

    sarIntegrationTestHelper.renderAndSaveReportAsPdf(renderResult, null, TEST_CRN)

    sarIntegrationTestHelper.assertHtmlEquals(renderResult, sarIntegrationTestHelper.getExpectedRenderResult())
  }
}
