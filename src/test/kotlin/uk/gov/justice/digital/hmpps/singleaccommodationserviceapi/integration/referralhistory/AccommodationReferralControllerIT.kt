package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response.expectedGetReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.Instant

class AccommodationReferralControllerIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `fetchAllReferralsAggregated aggregates results and sorts them by date descending`() {
    val crn = "X12345"

    val cas1Response = buildReferralHistory(
      casService = CasService.CAS1,
      createdAt = Instant.parse("2025-03-01T00:00:00Z"),
      status = Cas1AssessmentStatus.IN_PROGRESS,
    )
    val cas2Response = buildReferralHistory(
      casService = CasService.CAS2,
      createdAt = Instant.parse("2025-01-01T00:00:00Z"),
      status = Cas2Status.AWAITING_DECISION,
    )
    val cas2v2Response = buildReferralHistory(
      casService = CasService.CAS2v2,
      createdAt = Instant.parse("2025-04-01T00:00:00Z"),
      status = Cas2Status.PLACE_OFFERED,
    )
    val cas3Response = buildReferralHistory(
      casService = CasService.CAS3,
      createdAt = Instant.parse("2025-02-01T00:00:00Z"),
      status = TemporaryAccommodationAssessmentStatus.IN_REVIEW,
    )

    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS1, crn, cas1Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2, crn, cas2Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS2v2, crn, cas2v2Response)
    ApprovedPremisesStubs.getReferralOKResponse(CasService.CAS3, crn, cas3Response)

    restTestClient.get().uri("/cases/{crn}/applications", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetReferralHistory(
            id1 = cas1Response.first().id,
            id2 = cas2Response.first().id,
            id3 = cas2v2Response.first().id,
            id4 = cas3Response.first().id,
          ),
        )
      }
  }
}
