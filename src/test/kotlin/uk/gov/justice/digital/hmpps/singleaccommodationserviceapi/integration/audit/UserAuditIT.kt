package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.audit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.PersonName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.StaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
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

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    proposedAccommodationRepository.deleteAll()
    userRepository.deleteAll()

    HmppsAuthStubs.stubGrantToken()
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = usernameOfNewDeliusUser,
      response = StaffDetail(
        username = usernameOfNewDeliusUser,
        email = usernameOfNewDeliusUser,
        telephoneNumber = null,
        name = PersonName(
          forename = "Jane",
          surname = "Doe",
        ),
        code = "12345",
        active = true,
      ),
    )
    createTestDataSetupUserAndDeliusUser()
  }

  @AfterEach
  fun teardown() {
    proposedAccommodationRepository.deleteAll()
    userRepository.deleteAll()
  }

  @Test
  fun `should audit data properly on create and update`() {
    val createdEntity = createAndAssertAuditData()
    updateAndAssertAuditData(
      createdProposedAccommodationEntityId = createdEntity.id,
    )
  }

  private fun createAndAssertAuditData(): ProposedAccommodationEntity {
    restTestClient.post().uri("/cases/$crn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findByCrn(crn)
    assertThat(proposedAccommodationPersistedResult).isNotNull
    assertPersistedProposedAccommodationAuditFields(
      persistedProposedAccommodationEntity = proposedAccommodationPersistedResult!!,
      expectedCreatedByUserId = userIdOfLoggedInDeliusUser,
      expectedUpdatedByUserId = userIdOfLoggedInDeliusUser,
    )
    assertThat(proposedAccommodationPersistedResult.lastUpdatedAt).isEqualTo(proposedAccommodationPersistedResult.createdAt)
    return proposedAccommodationPersistedResult
  }

  private fun updateAndAssertAuditData(createdProposedAccommodationEntityId: UUID) {
    val newUserWhoShouldNotExistYet = userRepository.findByUsernameAndAuthSource(usernameOfNewDeliusUser, AuthSource.DELIUS)
    assertThat(newUserWhoShouldNotExistYet).isNull()

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/$createdProposedAccommodationEntityId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withUnknownDeliusUserJwt(username = usernameOfNewDeliusUser)
      .exchangeSuccessfully()

    val newUserShouldNowExist = userRepository.findByUsernameAndAuthSource(usernameOfNewDeliusUser.uppercase(), AuthSource.DELIUS)
    assertThat(newUserShouldNowExist).isNotNull
    val updatedEntity = proposedAccommodationRepository.findByIdOrNull(createdProposedAccommodationEntityId)
    assertThat(updatedEntity).isNotNull

    assertPersistedProposedAccommodationAuditFields(
      persistedProposedAccommodationEntity = updatedEntity!!,
      expectedCreatedByUserId = userIdOfLoggedInDeliusUser,
      expectedUpdatedByUserId = newUserShouldNowExist!!.id,
    )
    assertThat(updatedEntity.lastUpdatedAt).isAfter(updatedEntity.createdAt)
  }

  private fun assertPersistedProposedAccommodationAuditFields(
    persistedProposedAccommodationEntity: ProposedAccommodationEntity,
    expectedCreatedByUserId: UUID,
    expectedUpdatedByUserId: UUID,
  ) {
    assertThat(persistedProposedAccommodationEntity.createdByUserId).isEqualTo(expectedCreatedByUserId)
    assertThat(persistedProposedAccommodationEntity.lastUpdatedByUserId).isEqualTo(expectedUpdatedByUserId)
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
