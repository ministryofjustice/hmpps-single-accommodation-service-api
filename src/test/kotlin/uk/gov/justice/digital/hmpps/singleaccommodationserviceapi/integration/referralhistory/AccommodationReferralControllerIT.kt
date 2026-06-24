package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response.expectedGetReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.Instant
import java.time.LocalDate

class AccommodationReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  private lateinit var case: CaseEntity

  @BeforeEach
  fun setup() {
    case = caseRepository.save(buildCaseEntity { withCrn("X12345") })
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `fetchAllReferralsAggregated aggregates results and sorts them by date descending`() {
    val crn = "X12345"
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val dutyToRefer = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = DtrStatus.WITHDRAWN,
      ),
    )
    val referredByUser = buildDeliusUserDto()

    val cas1Response: List<Cas1ReferralHistory> = listOf(
      buildReferralHistory(
        createdAt = Instant.parse("2025-03-01T00:00:00Z"),
        status = Cas1AssessmentStatus.IN_PROGRESS,
        referredBy = referredByUser,
      ),
    )

    val cas3Response: List<Cas3ReferralHistory> = listOf(
      buildReferralHistory(
        createdAt = Instant.parse("2025-02-01T00:00:00Z"),
        status = TemporaryAccommodationAssessmentStatus.IN_REVIEW,
        referredBy = referredByUser,
      ),
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, cas1Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, cas3Response)

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetReferralHistory(
            id1 = cas1Response.first().id,
            id4 = cas3Response.first().id,
            dtrId = dutyToRefer.id,
            dtrStatus = "WITHDRAWN",
            dtrSubmissionDate = "2026-01-15",
          ),
        )
      }
  }
}
