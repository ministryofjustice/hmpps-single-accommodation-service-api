package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.customcaselist

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummaryName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationAccessControlStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_USER_CUSTOM_CASE_LIST
import java.time.LocalDate
import java.util.UUID

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
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns("B654321")

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
  fun `enriches a newly created case with the details from the case summary`() {
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
      response = CaseSummaries(
        listOf(
          buildCaseSummary(
            crn = "B654321",
            nomsId = "B1234BB",
            name = buildCaseSummaryName(forename = "Joe", surname = "Bloggs"),
            dateOfBirth = LocalDate.of(1990, 1, 2),
          ),
        ),
      ),
    )

    postCustomCaseList(listOf("B654321")).expectStatus().isCreated

    val newCase = caseRepository.findByCrn("B654321")
    assertThat(newCase).isNotNull()
    assertThat(newCase!!.firstName).isEqualTo("Joe")
    assertThat(newCase.lastName).isEqualTo("Bloggs")
    assertThat(newCase.dateOfBirth).isEqualTo(LocalDate.of(1990, 1, 2))
    assertThat(caseRepository.findByIdentifier("B1234BB", IdentifierType.PRISON_NUMBER)?.id).isEqualTo(newCase.id)
  }

  @Test
  fun `leaves an already persisted case untouched rather than enriching it`() {
    val existingCase = caseRepository.save(
      buildCaseEntity(firstName = "Joe", lastName = "Bloggs") { withCrn("A123456") },
    )

    postCustomCaseList(listOf("A123456")).expectStatus().isCreated

    val unchanged = caseRepository.findByCrn("A123456")
    assertThat(unchanged!!.id).isEqualTo(existingCase.id)
    assertThat(unchanged.firstName).isEqualTo("Joe")
    assertThat(unchanged.lastName).isEqualTo("Bloggs")
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
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns("A123456")

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
    val crns = (1..500).map { "A%06d".format(it) }
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns(*crns.toTypedArray())

    postCustomCaseList(crns).expectStatus().isCreated

    assertThat(userCustomCaseListRepository.findAll()).hasSize(500)
  }

  @Test
  fun `returns BadRequest and creates nothing when delius does not recognise a crn`() {
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns("A123456")

    postCustomCaseList(listOf("A123456", "B654321", "C111111")).expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Domain exception: invalidCrns: B654321, C111111")

    assertThat(caseRepository.findAll()).isEmpty()
    assertThat(userCustomCaseListRepository.findAll()).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  @Test
  fun `only sends crns that are not already persisted to delius for validation`() {
    caseRepository.save(buildCaseEntity { withCrn("A123456") })
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns("B654321")

    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().isCreated

    sasWiremock.verify(
      1,
      postRequestedFor(urlPathEqualTo("/probation-cases/summaries")).withRequestBody(equalToJson("[\"B654321\"]")),
    )
  }

  @Test
  fun `does not call delius when every crn is already persisted`() {
    caseRepository.save(buildCaseEntity { withCrn("A123456") })
    caseRepository.save(buildCaseEntity { withCrn("B654321") })

    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().isCreated

    sasWiremock.verify(0, postRequestedFor(urlPathEqualTo("/probation-cases/summaries")))
  }

  @Test
  fun `retries then creates nothing when delius is unavailable`() {
    ProbationIntegrationDeliusStubs.postCaseSummariesServerError()

    postCustomCaseList(listOf("A123456", "B654321")).expectStatus().is5xxServerError

    sasWiremock.verify(3, postRequestedFor(urlPathEqualTo("/probation-cases/summaries")))
    assertThat(caseRepository.findAll()).isEmpty()
    assertThat(userCustomCaseListRepository.findAll()).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
  }

  @Test
  fun `leaves the existing custom case list untouched when a crn is invalid`() {
    val existingCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })
    userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = deliusUser.id, sasCaseId = existingCase.id))
    ProbationIntegrationDeliusStubs.postCaseSummariesForCrns()

    postCustomCaseList(listOf("B654321")).expectStatus().isBadRequest

    val savedMappings = userCustomCaseListRepository.findAll()
    assertThat(savedMappings.map { it.sasCaseId }).containsExactly(existingCase.id)
    assertThat(caseRepository.findByCrn("B654321")).isNull()
  }

  @Nested
  inner class GetCustomCaseList {

    @BeforeEach
    fun stubCaseSummaries() {
      ProbationIntegrationDeliusStubs.postCaseSummariesForCrns("A123456", "B654321", "C111111")
    }

    @Test
    fun `returns an empty list when the user has no custom case list`() {
      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(0)

      sasWiremock.verify(0, postRequestedFor(urlPathEqualTo("/user/$USERNAME_OF_LOGGED_IN_DELIUS_USER/access")))
      sasWiremock.verify(0, postRequestedFor(urlPathEqualTo("/probation-cases/summaries")))
    }

    @Test
    fun `returns the persisted assigned to and the limited access flag from the case summary`() {
      val case = caseRepository.save(
        buildCaseEntity(
          assignedToForename = "Joe",
          assignedToSurname = "Bloggs",
          assignedToUsername = "JOE.BLOGGS",
        ) { withCrn("A123456") },
      )
      addToCustomCaseList(case.id)
      ProbationAccessControlStubs.postUserAccessForCrns(USERNAME_OF_LOGGED_IN_DELIUS_USER, accessibleCrns = listOf("A123456"))
      ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
        CaseSummaries(listOf(buildCaseSummary(crn = "A123456", currentRestriction = true))),
      )

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data[0].assignedTo.forename").isEqualTo("Joe")
        .jsonPath("$.data[0].assignedTo.surname").isEqualTo("Bloggs")
        .jsonPath("$.data[0].assignedTo.username").isEqualTo("JOE.BLOGGS")
        .jsonPath("$.data[0].limitedAccess").isEqualTo(true)
        .jsonPath("$.upstreamFailures").doesNotExist()

      sasWiremock.verify(
        1,
        postRequestedFor(urlPathEqualTo("/probation-cases/summaries")).withRequestBody(equalToJson("[\"A123456\"]")),
      )
    }

    @Test
    fun `returns the cases with an upstream failure and no limited access flag when delius case summaries are unavailable`() {
      addToCustomCaseList(caseRepository.save(buildCaseEntity(firstName = "Joe") { withCrn("A123456") }).id)
      ProbationAccessControlStubs.postUserAccessForCrns(USERNAME_OF_LOGGED_IN_DELIUS_USER, accessibleCrns = listOf("A123456"))
      ProbationIntegrationDeliusStubs.postCaseSummariesServerError()

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(1)
        .jsonPath("$.data[0].crn").isEqualTo("A123456")
        .jsonPath("$.data[0].forename").isEqualTo("Joe")
        .jsonPath("$.data[0].userAccess").isEqualTo("FULL")
        .jsonPath("$.data[0].limitedAccess").doesNotExist()
        .jsonPath("$.upstreamFailures.length()").isEqualTo(1)
        .jsonPath("$.upstreamFailures[0].endpoint").isEqualTo("postCaseSummaries")

      sasWiremock.verify(3, postRequestedFor(urlPathEqualTo("/probation-cases/summaries")))
    }

    @Test
    fun `returns the cases in the users custom case list`() {
      val case = caseRepository.save(
        buildCaseEntity(tierScore = "B2", firstName = "Joe", lastName = "Bloggs", roshLevelCode = "RHRH") { withCrn("A123456") },
      )
      addToCustomCaseList(case.id)
      ProbationAccessControlStubs.postUserAccessForCrns(USERNAME_OF_LOGGED_IN_DELIUS_USER, accessibleCrns = listOf("A123456"))

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(1)
        .jsonPath("$.data[0].crn").isEqualTo("A123456")
        .jsonPath("$.data[0].forename").isEqualTo("Joe")
        .jsonPath("$.data[0].surname").isEqualTo("Bloggs")
        .jsonPath("$.data[0].tierScore").isEqualTo("B2")
        .jsonPath("$.data[0].riskLevel").isEqualTo("HIGH")
        .jsonPath("$.data[0].userAccess").isEqualTo("FULL")

      sasWiremock.verify(
        1,
        postRequestedFor(urlPathEqualTo("/user/$USERNAME_OF_LOGGED_IN_DELIUS_USER/access")).withRequestBody(equalToJson("[\"A123456\"]")),
      )
    }

    @Test
    fun `does not return cases from another users custom case list`() {
      val user2 = userRepository.save(buildUserEntity(username = "user2"))
      val myCase = caseRepository.save(buildCaseEntity { withCrn("A123456") })
      val theirCase = caseRepository.save(buildCaseEntity { withCrn("B654321") })
      addToCustomCaseList(myCase.id)
      userCustomCaseListRepository.save(buildUserCustomCaseListEntity(sasUserId = user2.id, sasCaseId = theirCase.id))
      ProbationAccessControlStubs.postUserAccessForCrns(USERNAME_OF_LOGGED_IN_DELIUS_USER, accessibleCrns = listOf("A123456"))

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(1)
        .jsonPath("$.data[0].crn").isEqualTo("A123456")
    }

    @Test
    fun `returns cases the user is excluded or restricted from as limited cases at the top of the list`() {
      addToCustomCaseList(
        caseRepository.save(
          buildCaseEntity(
            firstName = "Joe",
            lastName = "Bloggs",
            accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED),
          ) { withCrn("A123456") },
        ).id,
      )
      addToCustomCaseList(caseRepository.save(buildCaseEntity(firstName = "Jane", lastName = "Doe") { withCrn("B654321") }).id)
      addToCustomCaseList(caseRepository.save(buildCaseEntity(firstName = "John", lastName = "Smith") { withCrn("C111111") }).id)
      ProbationAccessControlStubs.postUserAccessForCrns(
        USERNAME_OF_LOGGED_IN_DELIUS_USER,
        accessibleCrns = listOf("A123456"),
        excludedCrns = listOf("B654321"),
        restrictedCrns = listOf("C111111"),
      )

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(3)
        .jsonPath("$.data[0].crn").isEqualTo("B654321")
        .jsonPath("$.data[0].userAccess").isEqualTo("LIMITED")
        .jsonPath("$.data[0].limitedAccess").isEqualTo(true)
        .jsonPath("$.data[0].forename").doesNotExist()
        .jsonPath("$.data[1].crn").isEqualTo("C111111")
        .jsonPath("$.data[1].userAccess").isEqualTo("LIMITED")
        .jsonPath("$.data[1].surname").doesNotExist()
        .jsonPath("$.data[2].crn").isEqualTo("A123456")
        .jsonPath("$.data[2].userAccess").isEqualTo("FULL")
    }

    @Test
    fun `includes partially populated cases with blank fields`() {
      val partial = caseRepository.save(
        buildCaseEntity(tierScore = null, firstName = "Joe", lastName = "Bloggs") { withCrn("A123456") },
      )
      addToCustomCaseList(partial.id)
      ProbationAccessControlStubs.postUserAccessForCrns(USERNAME_OF_LOGGED_IN_DELIUS_USER, accessibleCrns = listOf("A123456"))

      getCustomCaseList().expectStatus().isOk
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(1)
        .jsonPath("$.data[0].crn").isEqualTo("A123456")
        .jsonPath("$.data[0].forename").isEqualTo("Joe")
        .jsonPath("$.data[0].tierScore").doesNotExist()
        .jsonPath("$.data[0].accommodationSummaries.caseAccommodationStatus").doesNotExist()
    }

    @Test
    fun `retries then fails when probation access control is unavailable`() {
      addToCustomCaseList(caseRepository.save(buildCaseEntity { withCrn("A123456") }).id)
      ProbationAccessControlStubs.postUserAccessServerError(USERNAME_OF_LOGGED_IN_DELIUS_USER)

      getCustomCaseList().expectStatus().is5xxServerError

      sasWiremock.verify(3, postRequestedFor(urlPathEqualTo("/user/$USERNAME_OF_LOGGED_IN_DELIUS_USER/access")))
    }

    private fun addToCustomCaseList(caseId: UUID) = userCustomCaseListRepository.save(
      buildUserCustomCaseListEntity(sasUserId = deliusUser.id, sasCaseId = caseId),
    )

    private fun getCustomCaseList() = restTestClient.get().uri("/case-list/custom")
      .withDeliusUserJwt()
      .exchange()
  }

  private fun postCustomCaseList(crns: List<String>) = restTestClient.post().uri("/case-list/custom")
    .contentType(MediaType.APPLICATION_JSON)
    .body(crns.joinToString(prefix = "[", postfix = "]") { "\"$it\"" })
    .withDeliusUserJwt()
    .exchange()
}
