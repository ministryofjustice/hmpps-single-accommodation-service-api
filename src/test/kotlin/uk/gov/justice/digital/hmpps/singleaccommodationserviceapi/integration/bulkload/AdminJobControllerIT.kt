package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.bulkload

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.bulkload.json.bulkLoadCasesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs

class AdminJobControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  private val teamCode = "TEAM1"
  private val teamCases = listOf(
    TeamCase(crn = "CRN1", prisonNumber = "PN1"),
    TeamCase(crn = "CRN2", prisonNumber = null),
  )
  private val crns = teamCases.map { it.crn }

  private val adminRoles = listOf("ROLE_SAS_ADMIN_RW")

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `should return 403 when the client does not have the admin role`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode)))
      .withClientCredentialsJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 if called with Delius user JWT`() {
    createDeliusUser()

    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode)))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should create the cases not already held and stage a bulk refresh for the team`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    assertThat(result.dryRun).isFalse()
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isZero()
    assertThat(result.casesCreated).isEqualTo(2)
    assertThat(result.refreshesRequested).isEqualTo(2)
    assertThat(result.errors).isEmpty()

    assertThat(caseRepository.findUnpersistedCrns(crns.toTypedArray())).isEmpty()

    val persistedCases = caseRepository.findByCrns(crns)
    val refreshRequests = caseRefreshRequestRepository.findAll()
    assertThat(refreshRequests.map { it.caseId }).containsExactlyInAnyOrderElementsOf(persistedCases.map { it.id })
    assertThat(refreshRequests).allSatisfy {
      assertThat(it.priority).isEqualTo(CaseRefreshPriority.BULK)
      assertThat(it.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    }
  }

  @Test
  fun `should only count the cases not already held when some of the team is already persisted`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)
    bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))

    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isEqualTo(2)
    assertThat(result.casesCreated).isZero()
    assertThat(caseRepository.findByCrns(crns)).hasSize(2)
  }

  @Test
  fun `should report what a run would do without writing anything when dry run is not specified`() {
    SasAndDeliusStubs.stubGetCasesByTeamCode(teamCode, teamCases)

    val result = bulkLoadCases(bulkLoadCasesRequestBody(teamCodes = listOf(" team1 ")))

    assertThat(result.dryRun).isTrue()
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isZero()
    assertThat(result.casesCreated).isZero()
    assertThat(result.refreshesRequested).isZero()

    assertThat(caseRepository.findByCrns(crns)).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  @Test
  fun `should skip a team whose cases could not be retrieved and report the upstream failure`() {
    SasAndDeliusStubs.stubGetCasesByTeamCodeFailure(teamCode)

    val response = restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf(teamCode), dryRun = false))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchangeSuccessfully()
      .expectBody(object : ParameterizedTypeReference<ApiResponseDto<BulkLoadCasesResultDto>>() {})
      .returnResult()
      .responseBody!!

    assertThat(response.data.teamsProcessed).isZero()
    assertThat(response.data.crnsFound).isZero()
    assertThat(response.upstreamFailures).hasSize(1)
    assertThat(response.upstreamFailures.first().failureType).isEqualTo(UpstreamFailureType.UPSTREAM_HTTP_ERROR)
    assertThat(caseRepository.findByCrns(crns)).isEmpty()
  }

  @Test
  fun `should return 400 when no usable team codes are supplied`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = listOf("", "  ")))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchange()
      .expectStatus().isBadRequest

    assertThat(caseRepository.findAll()).isEmpty()
  }

  @Test
  fun `should return 400 when an empty team code list is supplied`() {
    restTestClient.post().uri("/admin/bulk-load-cases")
      .contentType(MediaType.APPLICATION_JSON)
      .body(bulkLoadCasesRequestBody(teamCodes = emptyList()))
      .withClientCredentialsJwt(roles = adminRoles)
      .exchange()
      .expectStatus().isBadRequest
  }

  private fun bulkLoadCases(requestBody: String) = restTestClient.post().uri("/admin/bulk-load-cases")
    .contentType(MediaType.APPLICATION_JSON)
    .body(requestBody)
    .withClientCredentialsJwt(roles = adminRoles)
    .exchangeSuccessfully()
    .expectBody(object : ParameterizedTypeReference<ApiResponseDto<BulkLoadCasesResultDto>>() {})
    .returnResult()
    .responseBody!!
    .data
}
