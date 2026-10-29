package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eventdetails

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables
import java.util.UUID

class EventDetailsControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  private lateinit var caseEntity: CaseEntity
  private lateinit var crn: String
  private val localAuthorityArea: LocalAuthorityAreaEntity by lazy {
    localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
  }

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(SasTables.DUTY_TO_REFER, SasTables.SAS_CASE)
    caseEntity = caseRepository.save(buildCaseEntity())
    crn = caseEntity.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `returns event details for a duty to refer with the probation integration role`() {
    val dtrEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = caseEntity.id,
        localAuthorityAreaId = localAuthorityArea.id,
      ),
    )

    webTestClient.get().uri("/event-details/duty-to-refers/{id}", dtrEntity.id)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__PROBATION_INTEGRATION"))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .value { body ->
        assertThatJson(body!!).matchesExpectedJson(
          """
            {
              "id":"${dtrEntity.id}",
              "crn":"$crn",
              "viewUrl":"https://single-accommodation-service-test.hmpps.service.justice.gov.uk/cases/$crn/dtr/${dtrEntity.id}/details"
            }
          """.trimIndent(),
        )
      }
  }

  @Test
  fun `returns not found when the duty to refer does not exist`() {
    val missingId = UUID.randomUUID()

    webTestClient.get().uri("/event-details/duty-to-refers/{id}", missingId)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__PROBATION_INTEGRATION"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `returns forbidden when the user does not have the probation integration role`() {
    webTestClient.get().uri("/event-details/duty-to-refers/${UUID.randomUUID()}")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isForbidden
  }
}
