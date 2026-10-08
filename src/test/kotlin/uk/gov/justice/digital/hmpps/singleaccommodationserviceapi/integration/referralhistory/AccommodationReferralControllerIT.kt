package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1SpaceBookingStatus.NOT_ARRIVED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.RequestForPlacementStatus.AWAITING_MATCH
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ExternalReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response.expectedGetReferralHistoryResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response.expectedReferralHistoryItem
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.LocalDate

class AccommodationReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  private lateinit var externalReferralRepository: ExternalReferralRepository

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
        date = LocalDate.parse("2025-03-01"),
        applicationStatus = ApprovedPremisesApplicationStatus.ASSESSMENT_IN_PROGRESS,
        placementStatus = NOT_ARRIVED,
        requestForPlacementStatus = AWAITING_MATCH,
        referredBy = referredByUser,
      ),
    )

    val cas2Response: List<Cas2ReferralHistory> = listOf(
      buildReferralHistory(
        applicationSubmittedDate = LocalDate.parse("2025-02-15"),
        applicationLastUpdatedDate = LocalDate.parse("2025-02-20"),
        applicationStatus = Cas2AssessmentStatus.CANCELLED,
        referredBy = referredByUser,
      ),
    )

    val cas3Response: List<Cas3ReferralHistory> = listOf(
      buildReferralHistory(
        date = LocalDate.parse("2025-02-01"),
        applicationStatus = ApplicationStatus.IN_PROGRESS,
        assessmentStatus = Cas3ReferralHistory.AssessmentStatus.READY_TO_PLACE,
        bookingStatus = Cas3ReferralHistory.Cas3BookingStatus.DEPARTED,
        referredBy = referredByUser,
      ),
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, cas1Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2, crn, cas2Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, cas3Response)

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetReferralHistoryResponseBody(
            listOf(
              expectedReferralHistoryItem(
                id = dutyToRefer.id,
                type = "DTR",
                status = "WITHDRAWN",
                date = "2026-01-15",
                referredByName = "Test Data Setup User",
                referredByUsername = "TEST_DATA_SETUP_USER",
                localAuthorityArea = "Aberdeen City",
                pdu = "Aberdeen City",
              ),
              expectedReferralHistoryItem(
                id = cas1Response.first().id,
                type = "CAS1",
                status = "NOT_ARRIVED",
                date = "2025-03-01",
                referredByName = "Joe Bloggs",
                referredByUsername = "user1",
                requestForPlacementStatus = "awaiting_match",
                placementStatus = "notArrived",
                uiUrl = "https://example.com/referral",
              ),
              expectedReferralHistoryItem(
                id = cas2Response.first().id,
                type = "CAS2",
                status = "CANCELLED",
                date = "2025-02-15",
                applicationLastUpdatedDate = "2025-02-20",
                referredByName = "Joe Bloggs",
                referredByUsername = null,
                uiUrl = "https://example.com/referral",
              ),
              expectedReferralHistoryItem(
                id = cas3Response.first().id,
                type = "CAS3",
                status = "DEPARTED",
                date = "2025-02-01",
                referredByName = "Joe Bloggs",
                referredByUsername = "user1",
                assessmentStatus = "ready_to_place",
                placementStatus = "departed",
                uiUrl = "https://example.com/referral",
              ),
            ),
          ),
        )
      }
  }

  @Test
  fun `fetchAllReferralsAggregated includes withdrawalReason for CAS1`() {
    val crn = "X12345"
    val referredByUser = buildDeliusUserDto()

    val cas1Response: List<Cas1ReferralHistory> = listOf(
      buildReferralHistory(
        date = LocalDate.parse("2025-03-01"),
        applicationStatus = ApprovedPremisesApplicationStatus.WITHDRAWN,
        withdrawalReason = "DuplicatePlacementRequest",
        referredBy = referredByUser,
      ),
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, cas1Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2, crn, emptyList())
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, emptyList())

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data[0].withdrawalReason").isEqualTo("DuplicatePlacementRequest")
  }

  @Test
  fun `getApplicationHistory includes rejected external referrals and excludes submitted and accepted ones`() {
    val crn = "X12345"

    val rejectedReferral = externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-REJECTED",
        submissionDate = LocalDate.of(2026, 9, 20),
        status = ExternalReferralStatus.REJECTED,
        organisationName = "Rejecting Charity",
        withdrawalReason = ExternalReferralWithdrawalReason.PERSON_NOT_SUITABLE,
        withdrawalNote = "Not suitable for this organisation",
      ),
    )
    externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-SUBMITTED",
        submissionDate = LocalDate.of(2026, 9, 25),
        status = ExternalReferralStatus.SUBMITTED,
      ),
    )
    externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-ACCEPTED",
        submissionDate = LocalDate.of(2026, 9, 28),
        status = ExternalReferralStatus.ACCEPTED,
      ),
    )

    val archivedReferral = externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-ARCHIVED",
        submissionDate = LocalDate.of(2026, 10, 10),
        status = ExternalReferralStatus.ARCHIVED,
        organisationName = "Another Charity",
        withdrawalReason = ExternalReferralWithdrawalReason.ANOTHER_REASON,
        withdrawalNote = "Person requires a longer stay",
      ),
    )

    val completedReferral = externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-COMPLETED",
        submissionDate = LocalDate.of(2026, 9, 22),
        status = ExternalReferralStatus.COMPLETED,
        organisationName = "Another Charity",
        withdrawalReason = ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE,
        withdrawalNote = "The offender was successfully placed in the accommodation",
      ),
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, emptyList())
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2, crn, emptyList())
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, emptyList())

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetReferralHistoryResponseBody(
            listOf(
              expectedReferralHistoryItem(
                id = archivedReferral.id,
                type = "ER",
                status = "ARCHIVED",
                date = "2026-10-10",
                referredByName = NAME_OF_TEST_DATA_SETUP_USER,
                referredByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
                withdrawalReason = "ANOTHER_REASON",
                withdrawalNote = "Person requires a longer stay",
              ),
              expectedReferralHistoryItem(
                id = completedReferral.id,
                type = "ER",
                status = "COMPLETED",
                date = "2026-09-22",
                referredByName = NAME_OF_TEST_DATA_SETUP_USER,
                referredByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
                withdrawalReason = "PLACEMENT_COMPLETE",
                withdrawalNote = "The offender was successfully placed in the accommodation",
              ),
              expectedReferralHistoryItem(
                id = rejectedReferral.id,
                type = "ER",
                status = "REJECTED",
                date = "2026-09-20",
                referredByName = NAME_OF_TEST_DATA_SETUP_USER,
                referredByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
                withdrawalReason = "PERSON_NOT_SUITABLE",
                withdrawalNote = "Not suitable for this organisation",
              ),
            ),
          ),
        )
      }
  }

  @Test
  fun `getApplicationHistory excludes rejected external referrals that have no outcome reason`() {
    val crn = "X12345"

    externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-REJECTED-NO-REASON",
        submissionDate = LocalDate.of(2026, 1, 20),
        status = ExternalReferralStatus.REJECTED,
        organisationName = "Rejecting Charity",
        withdrawalReason = null,
        withdrawalNote = null,
      ),
    )
    val completedReferral = externalReferralRepository.save(
      buildExternalReferralEntity(
        caseId = case.id,
        crn = crn,
        referenceNumber = "OA-REF-COMPLETED",
        submissionDate = LocalDate.of(2026, 9, 22),
        status = ExternalReferralStatus.COMPLETED,
        organisationName = "Another Charity",
        withdrawalReason = ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE,
        withdrawalNote = "The offender was successfully placed in the accommodation",
      ),
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, emptyList())
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2, crn, emptyList())
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, emptyList())

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetReferralHistoryResponseBody(
            listOf(
              expectedReferralHistoryItem(
                id = completedReferral.id,
                type = "ER",
                status = "COMPLETED",
                date = "2026-09-22",
                referredByName = NAME_OF_TEST_DATA_SETUP_USER,
                referredByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
                withdrawalReason = "PLACEMENT_COMPLETE",
                withdrawalNote = "The offender was successfully placed in the accommodation",
              ),
            ),
          ),
        )
      }
  }
}
