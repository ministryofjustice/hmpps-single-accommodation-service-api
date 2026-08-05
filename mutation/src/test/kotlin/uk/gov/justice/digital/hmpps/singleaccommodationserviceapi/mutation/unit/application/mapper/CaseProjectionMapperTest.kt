package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseProjectionMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseMutationOrchestrationDto
import java.util.UUID

class CaseProjectionMapperTest {

  @Test
  fun `create builds a new entity from the projection and identifiers`() {
    val crn = "X123456"
    val cas1Application = buildCas1Application(
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      placementStatus = Cas1PlacementStatus.UPCOMING,
    )

    val entity = CaseProjectionMapper.create(
      projection = buildCaseMutationOrchestrationDto(
        crn = crn,
        tier = buildTier("A1"),
        cas1Application = cas1Application,
      ),
      crn = crn,
      prisonNumber = "A1234AA",
    )

    assertAll(
      { assertThat(entity.tierScore).isEqualTo("A1") },
      { assertThat(entity.cas1ApplicationId).isEqualTo(cas1Application.id) },
      { assertThat(entity.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.PLACEMENT_ALLOCATED) },
      { assertThat(entity.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.PLACEMENT_BOOKED) },
      { assertThat(entity.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.UPCOMING) },
      { assertThat(entity.caseIdentifiers.map { it.identifier to it.identifierType }).containsAll(listOf(crn to IdentifierType.CRN, "A1234AA" to IdentifierType.PRISON_NUMBER)) },
    )
  }

  @Test
  fun `merge applies the projection onto an existing entity`() {
    val crn = "X123456"
    val caseEntity = buildCaseEntity(
      tierScore = "A1",
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
    ) { withCrn(crn) }

    val cas1Application = buildCas1Application(
      applicationStatus = Cas1ApplicationStatus.WITHDRAWN,
      requestForPlacementStatus = Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN,
      placementStatus = Cas1PlacementStatus.CANCELLED,
    )

    val mergedEntity = CaseProjectionMapper.merge(
      entity = caseEntity,
      projection = buildCaseMutationOrchestrationDto(
        crn = crn,
        tier = buildTier("A3S"),
        cas1Application = cas1Application,
      ),
    )

    assertAll(
      { assertThat(mergedEntity.id).isEqualTo(caseEntity.id) },
      { assertThat(mergedEntity.tierScore).isEqualTo("A3S") },
      { assertThat(mergedEntity.cas1ApplicationId).isEqualTo(cas1Application.id) },
      { assertThat(mergedEntity.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.WITHDRAWN) },
      { assertThat(mergedEntity.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN) },
      { assertThat(mergedEntity.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.CANCELLED) },
      { assertThat(mergedEntity.caseIdentifiers.map { it.identifier }).containsExactly(crn) },
    )
  }

  @Test
  fun `merge clears fields absent from the projection`() {
    val caseEntity = buildCaseEntity(
      tierScore = "A1",
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
    )

    val mergedEntity = CaseProjectionMapper.merge(
      entity = caseEntity,
      projection = buildCaseMutationOrchestrationDto(
        crn = "X123456",
        tier = null,
        cas1Application = null,
      ),
    )

    assertAll(
      { assertThat(mergedEntity.tierScore).isNull() },
      { assertThat(mergedEntity.cas1ApplicationId).isNull() },
      { assertThat(mergedEntity.cas1ApplicationApplicationStatus).isNull() },
      { assertThat(mergedEntity.cas1ApplicationRequestForPlacementStatus).isNull() },
      { assertThat(mergedEntity.cas1ApplicationPlacementStatus).isNull() },
    )
  }
}
