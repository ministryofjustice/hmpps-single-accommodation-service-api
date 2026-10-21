package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SubjectAccessRequestResponse
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class SasSarPdfReportTest : SasSarTestBase() {

  private val testCrn = "X320741"
  private val testCaseId = UUID.fromString("936a692e-e1e7-49b1-b44f-1c4497f74fe2")
  private val testPaId = UUID.fromString("3276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testPaId2 = UUID.fromString("4276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testCas1Id1 = UUID.fromString("5276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testCas1Id2 = UUID.fromString("6276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testCas3Id1 = UUID.fromString("7276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testCas3Id2 = UUID.fromString("8276ece4-4c1d-4a5f-a50e-ef92c1aee368")
  private val testDtrId = UUID.fromString("5e0fe44d-6903-42ce-a426-c7b53494ba35")
  private val testDtrId2 = UUID.fromString("6e0fe44d-6903-42ce-a426-c7b53494ba35")
  private val testLaaId = UUID.randomUUID()
  private val testLaaName = UUID.randomUUID().toString()
  private val testNomsNumber = "A1234BC"
  private val testFromDate: LocalDate = LocalDate.of(2019, 1, 1)
  private val testToDate: LocalDate = LocalDate.of(2026, 12, 31)
  private val fixedTimestamp = Timestamp.from(Instant.parse("2026-06-30T15:31:04Z"))

  private fun setupTestData() {
    transactionTemplate.execute {
      createTestDataSetupUserAndDeliusUser()
      HmppsAuthStubs.stubGrantToken()

      val case = createCase(testCrn, testNomsNumber, testCaseId)

      val laa = localAuthorityAreaRepository.save(buildLocalAuthorityAreaEntity(id = testLaaId, name = testLaaName))

      val dtrIds = listOf(testDtrId, testDtrId2)
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
        dtr.notes.add(
          buildDutyToReferNoteEntity(
            dutyToReferEntity = dtr,
            note = "DTR Note ${getLoremIpsum()} ${index + 1}",
            createdByUserId = userIdOfTestDataSetupUser,
          ),
        )
        dutyToReferRepository.save(dtr)
      }

      val accStatus = accommodationStatusRepository.findAll().first()
      val paConfigs = listOf(
        Triple(testPaId, "A10", "Proposed Accommodation 1"),
        Triple(testPaId2, "A11", "Proposed Accommodation 2"),
        Triple(testCas1Id1, "A02", "CAS1 Referral 1"),
        Triple(testCas1Id2, "A02", "CAS1 Referral 2"),
        Triple(testCas3Id1, "A17", "CAS3 Referral 1"),
        Triple(testCas3Id2, "A17", "CAS3 Referral 2"),
      )

      val paNoteIds = mutableListOf<UUID>()
      paConfigs.forEachIndexed { index, (id, typeCode, name) ->
        val accType =
          accommodationTypeRepository.findAll().find { it.code == typeCode } ?: accommodationTypeRepository.findAll()
            .first()
        val pa = buildProposedAccommodationEntity(
          id = id,
          caseId = case.id,
          name = name,
          accommodationTypeEntity = accType,
          accommodationStatusEntity = accStatus,
          createdByUserId = userIdOfTestDataSetupUser,
          lastUpdatedByUserId = userIdOfTestDataSetupUser,
        )
        val note = buildProposedAccommodationNoteEntity(
          proposedAccommodationEntity = pa,
          note = "PA Note ${getLoremIpsum()} ${index + 1}",
          createdByUserId = userIdOfTestDataSetupUser,
        )
        paNoteIds.add(note.id)
        pa.notes.add(note)

        proposedAccommodationRepository.save(pa)
      }

      entityManager.flush()

      // Force timestamps to match fixture
      dtrIds.forEach { id ->
        fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.DUTY_TO_REFER, id, fixedTimestamp)
      }
      paConfigs.forEach { (id, _, _) ->
        fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION, id, fixedTimestamp)
      }
      paNoteIds.forEach { noteId ->
        fixUpdatedAndCreatedTimestamps(DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION_NOTE, noteId, fixedTimestamp)
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
      testCrn,
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

    sarIntegrationTestHelper.renderAndSaveReportAsPdf(renderResult, null, testCrn)

    val expected = sarIntegrationTestHelper.getExpectedRenderResult().replace("%LAA_NAME%", testLaaName)
    sarIntegrationTestHelper.assertHtmlEquals(renderResult, expected)
  }
}
