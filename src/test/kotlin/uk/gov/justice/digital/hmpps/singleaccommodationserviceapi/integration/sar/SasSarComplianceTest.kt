package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class SasSarComplianceTest : SasSarTestBase() {

  private val testCrn = "X320741"
  private val testCaseId = UUID.fromString("936a692e-e1e7-49b1-b44f-1c4497f74fe2")
  private val testPaId = UUID.fromString("3276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testNoteId = UUID.fromString("1116ece4-4c1d-4a5f-a50e-ef92c1aee111")
  private val testDtrId = UUID.fromString("5e0fe44d-6903-42ce-a426-c7b53494ba35")
  private val testNomsNumber = "A1234BC"
  private val testFromDate: LocalDate = LocalDate.of(2019, 1, 1)
  private val testToDate: LocalDate = LocalDate.of(2026, 12, 31)

  private val expectedApiResponsePath = "/sar/sas-expected-api-response.json"
  private val expectedReportPath = "/sar/sas-expected-report.html"
  private val generatedApiResponseFilename = "sas-sar-api-response.json.log"
  private val generatedReportFilename = "sas-sar-report.html.log"

  private val fixedTimestamp = Timestamp.from(Instant.parse("2026-06-30T15:31:04Z"))

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
    transactionTemplate.execute {
      createTestDataSetupUserAndDeliusUser()
      HmppsAuthStubs.stubGrantToken()

      val case = createCase(testCrn, testNomsNumber, testCaseId)

      val dtr = dutyToReferRepository.save(
        buildDutyToReferEntity(
          id = testDtrId,
          caseId = case.id,
          localAuthorityAreaId = localAuthorityArea.id,
          createdByUserId = userIdOfTestDataSetupUser,
          lastUpdatedByUserId = userIdOfTestDataSetupUser,
        ),
      )
      dtr.notes.add(buildDutyToReferNoteEntity(dutyToReferEntity = dtr, createdByUserId = userIdOfTestDataSetupUser))
      dutyToReferRepository.save(dtr)

      val accType = accommodationTypeRepository.findAll().find { it.code == "A10" } ?: accommodationTypeRepository.findAll().first()
      val accStatus = accommodationStatusRepository.findAll().first()
      val pa = buildProposedAccommodationEntity(
        id = testPaId,
        caseId = case.id,
        accommodationTypeEntity = accType,
        accommodationStatusEntity = accStatus,
        createdByUserId = userIdOfTestDataSetupUser,
        lastUpdatedByUserId = userIdOfTestDataSetupUser,
      )
      pa.notes.add(buildProposedAccommodationNoteEntity(id = testNoteId, proposedAccommodationEntity = pa, createdByUserId = userIdOfTestDataSetupUser))
      proposedAccommodationRepository.save(pa)

      entityManager.flush()

      // Force timestamps to match fixture
      fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.DUTY_TO_REFER, testDtrId, fixedTimestamp)
      fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION, testPaId, fixedTimestamp)
      fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION_NOTE, testNoteId, fixedTimestamp)
    }
  }

  @Test
  fun `SAS SAR API should return consolidated data for all domains`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = testCrn,
      fromDate = testFromDate,
      toDate = testToDate,
      laaId = localAuthorityArea.id,
      laaName = localAuthorityArea.name,
    )
  }

  @Test
  fun `SAS SAR API should return consolidated data for all domains, no date range`() {
    setupTestData()
    asserter.assertApiDataMatchesFixture(
      crn = testCrn,
      fromDate = null,
      toDate = null,
      laaId = localAuthorityArea.id,
      laaName = localAuthorityArea.name,
    )
  }

  @Test
  fun `SAS SAR report should render correctly with all domains`() {
    setupTestData()
    sarIntegrationTestHelper.stubFindUserLastNameWith("Johnson")
    asserter.assertReportMatchesFixture(
      crn = testCrn,
      fromDate = testFromDate,
      toDate = testToDate,
      laaName = localAuthorityArea.name,
    )
  }
}
