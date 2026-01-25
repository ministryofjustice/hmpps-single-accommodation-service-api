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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.expectedSasAddressUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json.proposedAddressesRequestBody
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

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
  fun `should create proposed-accommodation and publish sas-address-updated event`() {
    val result = mockMvc.perform(
      post("/cases/$crn/proposed-accommodations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          proposedAddressesRequestBody(
            accommodationStatus = AccommodationStatus.PASSED.name,
          ),
        ),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.getByCrn(crn)!!
    assertPersistedProposedAccommodation(proposedAccommodationPersistedResult)
    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = proposedAccommodationPersistedResult.id,
        accommodationStatus = AccommodationStatus.PASSED.name,
        createdAt = proposedAccommodationPersistedResult.createdAt.toCanonicalString(),
      ),
    )
    assertPublishedSNSEvent(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_ADDRESS_UPDATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_ADDRESS_UPDATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
    )
  }

  private fun assertPersistedProposedAccommodation(proposedAccommodationEntity: ProposedAccommodationEntity) {
    assertThat(proposedAccommodationEntity.name).isEqualTo("Mother's caravan")
    assertThat(proposedAccommodationEntity.arrangementType).isEqualTo(AccommodationArrangementType.PRIVATE)
    assertThat(proposedAccommodationEntity.arrangementSubType).isEqualTo(AccommodationArrangementSubType.OTHER)
    assertThat(proposedAccommodationEntity.arrangementSubTypeDescription).isEqualTo("Caravan site")
    assertThat(proposedAccommodationEntity.settledType).isEqualTo(AccommodationSettledType.SETTLED)
    assertThat(proposedAccommodationEntity.offenderReleaseType).isEqualTo(OffenderReleaseType.REMAND)
    assertThat(proposedAccommodationEntity.status).isEqualTo(AccommodationStatus.PASSED)
    assertThat(proposedAccommodationEntity.postcode).isEqualTo("test postcode")
    assertThat(proposedAccommodationEntity.subBuildingName).isEqualTo("test sub building name")
    assertThat(proposedAccommodationEntity.buildingName).isEqualTo("test building name")
    assertThat(proposedAccommodationEntity.buildingNumber).isEqualTo("4")
    assertThat(proposedAccommodationEntity.throughfareName).isEqualTo("test thoroughfareName")
    assertThat(proposedAccommodationEntity.dependentLocality).isEqualTo("test dependent locality")
    assertThat(proposedAccommodationEntity.postTown).isEqualTo("test post town")
    assertThat(proposedAccommodationEntity.county).isEqualTo("test county")
    assertThat(proposedAccommodationEntity.uprn).isEqualTo("UP123454")
    assertThat(proposedAccommodationEntity.startDate).isEqualTo(LocalDate.of(2026, 1, 5))
    assertThat(proposedAccommodationEntity.endDate).isEqualTo(LocalDate.of(2026, 4, 25))
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
    assertThat(outboxRecord.domainEventType).isEqualTo(SingleAccommodationServiceDomainEventType.SAS_ADDRESS_UPDATED.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedSasAddressUpdatedDomainEventJson(proposedAccommodationId))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
