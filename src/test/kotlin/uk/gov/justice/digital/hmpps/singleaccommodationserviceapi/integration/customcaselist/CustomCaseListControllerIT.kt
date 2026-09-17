package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.customcaselist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_USER_CUSTOM_CASE_LIST

class CustomCaseListControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var userCustomCaseListRepository: UserCustomCaseListRepository

  @Autowired
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  lateinit var deliusUser: UserEntity

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(SAS_USER_CUSTOM_CASE_LIST, SAS_CASE)
    deliusUser = createTestDataSetupUserAndDeliusUser().second
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `creates the custom case list and creates then refreshes any crns not already persisted`() {
    val existingCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })

    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().isCreated

    val savedMappings = userCustomCaseListRepository.findAll()
    assertThat(savedMappings).hasSize(2)

    val newCase = caseRepository.findByCrn("B654321")
    assertThat(newCase).isNotNull()
    assertThat(savedMappings.map { it.sasCaseId }).containsExactlyInAnyOrder(existingCase.id, newCase!!.id)

    val refreshRequests = caseRefreshRequestRepository.findAll()
    assertThat(refreshRequests.map { it.caseId }).containsExactlyInAnyOrder(existingCase.id, newCase.id)
  }

  @Test
  fun `replaces an existing custom case list on POST`() {
    val oldCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })
    val newCase = caseRepository.save(buildCaseEntity { withCrn("B654321") })
    userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = deliusUser.id, sasCaseId = oldCase.id))

    postCustomCaseList(listOf("B654321")).expectStatus().isCreated

    val savedMappings = userCustomCaseListRepository.findAll()
    assertThat(savedMappings).hasSize(1)
    assertThat(savedMappings.single().sasCaseId).isEqualTo(newCase.id)
  }

  @Test
  fun `replaces an existing custom case list that overlaps the new one`() {
    val keptCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })
    val droppedCase = caseRepository.save(buildCaseEntity { withCrn("B654321") })
    caseRepository.save(buildCaseEntity { withCrn("C111111") })
    userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = deliusUser.id, sasCaseId = keptCase.id))
    userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = deliusUser.id, sasCaseId = droppedCase.id))

    postCustomCaseList(listOf("A123456", "C111111")).expectStatus().isCreated

    val addedCase = caseRepository.findByCrn("C111111")!!
    val savedMappings = userCustomCaseListRepository.findAll()
    assertThat(savedMappings.map { it.sasCaseId }).containsExactlyInAnyOrder(keptCase.id, addedCase.id)
    assertThat(savedMappings.map { it.sasUserId }).containsOnly(deliusUser.id)
  }

  @Test
  fun `resubmitting the identical custom case list succeeds and leaves one mapping per case`() {
    caseRepository.save(buildCaseEntity { withCrn("A123456") })
    caseRepository.save(buildCaseEntity { withCrn("B654321") })

    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().isCreated
    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().isCreated

    assertThat(userCustomCaseListRepository.findAll()).hasSize(2)
  }

  @Test
  fun `replacing one users list does not affect another users list`() {
    val user2 = userRepository.save(buildUserEntity(username = "user2"))
    val sharedCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })
    userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = user2.id, sasCaseId = sharedCase.id))

    postCustomCaseList(listOf("A123456")).expectStatus().isCreated

    val otherUsersMappings = userCustomCaseListRepository.findAll().filter { it.sasUserId == user2.id }
    assertThat(otherUsersMappings.map { it.sasCaseId }).containsExactly(sharedCase.id)
  }

  @Test
  fun `collapses duplicate crns in one request`() {
    caseRepository.save(buildCaseEntity { withCrn("A123456") })

    postCustomCaseList(listOf("A123456", "a123456", " A123456 ")).expectStatus().isCreated

    assertThat(userCustomCaseListRepository.findAll()).hasSize(1)
  }

  @Test
  fun `accepts a lowercase crn and stores it against the uppercased case`() {
    postCustomCaseList(listOf("a123456")).expectStatus().isCreated

    assertThat(caseRepository.findByCrn("A123456")).isNotNull()
    assertThat(caseRepository.findByCrn("a123456")).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["123456", "AB12345", "A12345", "A1234567", "A12B456"])
  fun `returns BadRequest when a crn format is invalid`(crn: String) {
    postCustomCaseList(listOf(crn)).expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Validation failure: CRN must be in format A123456: $crn")

    assertThat(userCustomCaseListRepository.findAll()).isEmpty()
  }

  @Test
  fun `returns BadRequest for an empty crn list`() {
    postCustomCaseList(emptyList()).expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Validation failure: createCustomCaseList.crns: Between 1 and 500 CRNs must be provided")
  }

  @Test
  fun `returns BadRequest when more than 500 crns are provided`() {
    postCustomCaseList(List(501) { "A123456" }).expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Validation failure: createCustomCaseList.crns: Between 1 and 500 CRNs must be provided")

    assertThat(userCustomCaseListRepository.findAll()).isEmpty()
  }

  @Test
  fun `accepts exactly 500 crns`() {
    postCustomCaseList((1..500).map { "A%06d".format(it) }).expectStatus().isCreated

    assertThat(userCustomCaseListRepository.findAll()).hasSize(500)
  }

  private fun postCustomCaseList(crns: List<String>) = restTestClient.post().uri("/case-list/custom")
    .contentType(MediaType.APPLICATION_JSON)
    .body(crns.joinToString(prefix = "[", postfix = "]") { "\"$it\"" })
    .withDeliusUserJwt()
    .exchange()
}
