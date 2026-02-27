package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.audit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class UserAuditIT : IntegrationTestBase() {

  @Autowired
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  private val crn = "FAKECRN1"
  private val usernameOfNewDeliusUser = "newDeliusUser@justice.gov.uk"

  private lateinit var beforeTest: Instant

  private val staffDetail = buildStaffDetail(
    username = usernameOfNewDeliusUser,
    email = usernameOfNewDeliusUser,
  )

  private val nomisUserDetail = buildNomisUserDetail(
    USERNAME_OF_LOGGED_IN_NOMIS_USER,
    primaryEmail = USERNAME_OF_LOGGED_IN_NOMIS_USER,
  )

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    proposedAccommodationRepository.deleteAll()
    userRepository.deleteAll()

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @AfterEach
  fun teardown() {
    proposedAccommodationRepository.deleteAll()
    userRepository.deleteAll()
  }

  @Test
  fun `should audit data properly for Delius User on create and update`() {
    val createdEntity = createAndAssertAuditData(
      authSource = AuthSource.DELIUS,
      withJwt = { withDeliusUserJwt() },
      expectedCreatedByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    )
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = usernameOfNewDeliusUser,
      response = staffDetail,
    )
    updateAndAssertAuditData(
      createdProposedAccommodationEntityId = createdEntity.id,
      withJwt = { withDeliusUserJwt(username = usernameOfNewDeliusUser) },
      expectedCreatedByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      expectedUpdatedByUsername = usernameOfNewDeliusUser,
    )
    assertThatUsersTableHasExpectedUsers(
      usernameOfExpectedExtraUser = usernameOfNewDeliusUser.uppercase(),
      authSourceOfExpectedExtraUser = AuthSource.DELIUS,
    )
  }

  @Test
  fun `should audit data properly for Nomis User`() {
    NomisUserRolesStubs.stubMe(
      jwt = jwtAuthHelper.createJwtAccessToken(
        USERNAME_OF_LOGGED_IN_NOMIS_USER,
        roles = listOf("ROLE_POM", "ROLE_PRISON"),
        authSource = AuthSource.NOMIS.value,
      ),
      response = nomisUserDetail,
    )
    createAndAssertAuditData(
      authSource = AuthSource.NOMIS,
      withJwt = { withNomisUserJwt() },
      expectedCreatedByUsername = USERNAME_OF_LOGGED_IN_NOMIS_USER,
    )
    assertThatUsersTableHasExpectedUsers(
      usernameOfExpectedExtraUser = USERNAME_OF_LOGGED_IN_NOMIS_USER,
      authSourceOfExpectedExtraUser = AuthSource.NOMIS,
    )
  }

  fun assertThatUsersTableHasExpectedUsers(
    usernameOfExpectedExtraUser: String,
    authSourceOfExpectedExtraUser: AuthSource,
  ) {
    val users = userRepository.findAll()
    assertThat(users).hasSize(3)
    val unknownUser = users.firstOrNull { it.username == usernameOfExpectedExtraUser }
    assertThat(unknownUser).isNotNull
    if (AuthSource.DELIUS == authSourceOfExpectedExtraUser) {
      assertThatUnknownDeliusUserWasPersistedAsExpected(unknownUser!!)
    } else {
      assertThatUnknownNomisUserWasPersistedAsExpected(unknownUser!!)
    }
  }

  fun assertThatUnknownDeliusUserWasPersistedAsExpected(
    unknownUser: UserEntity,
  ) {
    assertThat(unknownUser.authSource).isEqualTo(AuthSource.DELIUS)
    assertThat(unknownUser.username).isEqualTo(staffDetail.username!!.uppercase())
    assertThat(unknownUser.email).isEqualTo(staffDetail.email)
    assertThat(unknownUser.name).isEqualTo(staffDetail.name.deliusName())
    assertThat(unknownUser.telephoneNumber).isEqualTo(staffDetail.telephoneNumber)
    assertThat(unknownUser.deliusStaffCode).isEqualTo(staffDetail.code)
    assertThat(unknownUser.nomisStaffId).isNull()
    assertThat(unknownUser.nomisAccountType).isNull()
    assertThat(unknownUser.nomisActiveCaseloadId).isNull()
    assertThat(unknownUser.isEnabled).isTrue
    assertThat(unknownUser.isActive).isTrue
  }

  fun assertThatUnknownNomisUserWasPersistedAsExpected(
    unknownUser: UserEntity,
  ) {
    assertThat(unknownUser.authSource).isEqualTo(AuthSource.NOMIS)
    assertThat(unknownUser.username).isEqualTo(nomisUserDetail.username)
    assertThat(unknownUser.name).isEqualTo("${nomisUserDetail.firstName} ${nomisUserDetail.lastName}")
    assertThat(unknownUser.email).isEqualTo(nomisUserDetail.primaryEmail)
    assertThat(unknownUser.nomisStaffId).isEqualTo(nomisUserDetail.staffId)
    assertThat(unknownUser.nomisAccountType).isEqualTo(nomisUserDetail.accountType)
    assertThat(unknownUser.nomisActiveCaseloadId).isEqualTo(nomisUserDetail.activeCaseloadId)
    assertThat(unknownUser.isEnabled).isEqualTo(nomisUserDetail.active)
    assertThat(unknownUser.isActive).isEqualTo(nomisUserDetail.enabled)
    assertThat(unknownUser.telephoneNumber).isNull()
    assertThat(unknownUser.deliusStaffCode).isNull()
  }

  private fun createAndAssertAuditData(
    authSource: AuthSource,
    withJwt: RestTestClient.RequestHeadersSpec<*>.() -> RestTestClient.RequestHeadersSpec<*>,
    expectedCreatedByUsername: String,
  ): ProposedAccommodationEntity {
    restTestClient.post().uri("/cases/$crn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withJwt()
      .exchangeSuccessfully()

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findByCrn(crn)
    assertThat(proposedAccommodationPersistedResult).isNotNull
    assertPersistedProposedAccommodationAuditFields(
      persistedProposedAccommodationEntity = proposedAccommodationPersistedResult!!,
      authSource,
      expectedCreatedByUsername,
    )
    assertThat(proposedAccommodationPersistedResult.lastUpdatedAt).isEqualTo(proposedAccommodationPersistedResult.createdAt)
    return proposedAccommodationPersistedResult
  }

  private fun updateAndAssertAuditData(
    createdProposedAccommodationEntityId: UUID,
    withJwt: RestTestClient.RequestHeadersSpec<*>.() -> RestTestClient.RequestHeadersSpec<*>,
    expectedCreatedByUsername: String,
    expectedUpdatedByUsername: String?,
  ) {
    val newUserWhoShouldNotExistYet = userRepository.findByUsernameAndAuthSource(usernameOfNewDeliusUser, authSource = AuthSource.DELIUS)
    assertThat(newUserWhoShouldNotExistYet).isNull()

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/$createdProposedAccommodationEntityId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withJwt()
      .exchangeSuccessfully()

    val newUserShouldNowExist = userRepository.findByUsernameAndAuthSource(usernameOfNewDeliusUser.uppercase(), authSource = AuthSource.DELIUS)
    assertThat(newUserShouldNowExist).isNotNull

    val updatedEntity = proposedAccommodationRepository.findByIdOrNull(createdProposedAccommodationEntityId)
    assertThat(updatedEntity).isNotNull

    assertPersistedProposedAccommodationAuditFields(
      persistedProposedAccommodationEntity = updatedEntity!!,
      AuthSource.DELIUS,
      expectedCreatedByUsername,
      expectedUpdatedByUsername,
    )
    assertThat(updatedEntity.lastUpdatedAt).isAfter(updatedEntity.createdAt)
  }

  private fun assertPersistedProposedAccommodationAuditFields(
    persistedProposedAccommodationEntity: ProposedAccommodationEntity,
    authSource: AuthSource,
    expectedCreatedByUsername: String,
    expectedUpdateByUsername: String? = null,
  ) {
    val createdByUser = userRepository.findByUsernameAndAuthSource(expectedCreatedByUsername, authSource)
    val updatedByUser = expectedUpdateByUsername?.let {
      userRepository.findByUsernameAndAuthSource(expectedUpdateByUsername.uppercase(), authSource)
    } ?: createdByUser
    assertThat(persistedProposedAccommodationEntity.createdByUserId).isEqualTo(createdByUser.id)
    assertThat(persistedProposedAccommodationEntity.lastUpdatedByUserId).isEqualTo(updatedByUser.id)
    assertThat(persistedProposedAccommodationEntity.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
    assertThat(persistedProposedAccommodationEntity.lastUpdatedAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
  }
}
