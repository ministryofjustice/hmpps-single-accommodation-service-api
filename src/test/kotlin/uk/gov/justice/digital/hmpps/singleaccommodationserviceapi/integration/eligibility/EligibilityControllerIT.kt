package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.PrisonerSearchStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import java.util.UUID

class EligibilityControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val prisonerNumber = "1234567"
  private val cas1ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111111")
  private val dutyToReferCaseId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111112")

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
        prisonNumbers = listOf(prisonerNumber),
      ),
    )
    val tier = buildTier(TierScore.A1)
    val cas1Application = buildCas1Application(id = cas1ApplicationId)
    val prisoner = buildPrisoner(prisonerNumber = prisonerNumber)
    val entity = buildCaseEntity(id = dutyToReferCaseId) { withCrn(crn) }

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)

    ApprovedPremisesStubs.getSuitableApplicationOKResponse(crn = crn, response = cas1Application)

    TierStubs.getTierOKResponse(crn = crn, tier)

    PrisonerSearchStubs.getPrisonerOKResponse(prisonerNumber = prisonerNumber, prisoner)
    caseRepository.save(entity)
  }

  @AfterEach
  fun teardown() {
    dutyToReferRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @Test
  fun `should get eligibility for crn`() {
    restTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetEligibilityResponse(crn, cas1ApplicationId, dutyToReferCaseId))
      }
  }
}
