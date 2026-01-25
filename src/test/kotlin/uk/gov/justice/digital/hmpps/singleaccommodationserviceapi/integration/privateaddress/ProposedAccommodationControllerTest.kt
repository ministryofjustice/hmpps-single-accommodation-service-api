package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.messaging.TestSqsDomainEventListener
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.expectedGetPrivateAddressesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.expectedProposedAccommodationCreatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.proposedAddressesRequestBody
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID
import kotlin.String

class ProposedAccommodationControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  private val crn = "X371199"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get proposed-accommodation for crn`() {
    val result = mockMvc
      .perform(get("/cases/$crn/proposed-accommodations"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedGetPrivateAddressesResponse())
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should create proposed-accommodation`() {
    val result = mockMvc.perform(
      post("/cases/$crn/proposed-accommodations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(proposedAddressesRequestBody()),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.getByCrn(crn)!!
    assertPersistedProposedAccommodation(proposedAccommodationPersistedResult)
    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(proposedAccommodationPersistedResult.id, proposedAccommodationPersistedResult.createdAt),
    )
    assertPublishedSNSEvent(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
      eventType = SingleAccommodationServiceDomainEventType.PROPOSED_ACCOMMODATION_CREATED,
      eventDescription = SingleAccommodationServiceDomainEventType.PROPOSED_ACCOMMODATION_CREATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
    )
  }

  private fun assertPersistedProposedAccommodation(proposedAccommodation: ProposedAccommodationEntity) {
    assertThat(proposedAccommodation.name).isEqualTo("Mother's caravan")
    assertThat(proposedAccommodation.arrangementType).isEqualTo(AccommodationArrangementType.PRIVATE)
    assertThat(proposedAccommodation.arrangementSubType).isEqualTo(AccommodationArrangementSubType.OTHER)
    assertThat(proposedAccommodation.arrangementSubTypeDescription).isEqualTo("Caravan site")
    assertThat(proposedAccommodation.settledType).isEqualTo(AccommodationSettledType.SETTLED)
    assertThat(proposedAccommodation.offenderReleaseType).isEqualTo(OffenderReleaseType.REMAND)
    assertThat(proposedAccommodation.status).isEqualTo(AccommodationStatus.NOT_CHECKED_YET)
    assertThat(proposedAccommodation.postcode).isEqualTo("test postcode")
    assertThat(proposedAccommodation.subBuildingName).isEqualTo("test sub building name")
    assertThat(proposedAccommodation.buildingName).isEqualTo("test building name")
    assertThat(proposedAccommodation.buildingNumber).isEqualTo("4")
    assertThat(proposedAccommodation.throughfareName).isEqualTo("test thoroughfareName")
    assertThat(proposedAccommodation.dependentLocality).isEqualTo("test dependent locality")
    assertThat(proposedAccommodation.postTown).isEqualTo("test post town")
    assertThat(proposedAccommodation.county).isEqualTo("test county")
    assertThat(proposedAccommodation.uprn).isEqualTo("UP123454")
    assertThat(proposedAccommodation.startDate).isEqualTo(LocalDate.of(2026, 1, 5))
    assertThat(proposedAccommodation.endDate).isEqualTo(LocalDate.of(2026, 4, 25))
  }

  private fun assertPublishedSNSEvent(
    proposedAccommodationId: UUID,
    eventType: SingleAccommodationServiceDomainEventType,
    eventDescription: String,
    detailUrl: String = "http://api-host/proposed-accommodation",
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(eventType)
    assertThat(emittedMessage.description).isEqualTo(eventDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo("$detailUrl/$proposedAccommodationId")
  }

  private fun assertThatOutboxIsAsExpected(proposedAccommodationId: UUID) {
    val outboxRecord = outboxEventRepository.findAll().first()
    assertThat(outboxRecord.aggregateId).isEqualTo(proposedAccommodationId)
    assertThat(outboxRecord.aggregateType).isEqualTo("ProposedAccommodation")
    assertThat(outboxRecord.domainEventType).isEqualTo(SingleAccommodationServiceDomainEventType.PROPOSED_ACCOMMODATION_CREATED.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedProposedAccommodationCreatedDomainEventJson(proposedAccommodationId))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
