package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

class CaseMapperTest {

  @Test
  fun `toAggregate maps all fields correctly`() {
    val caseEntity = buildCaseEntity(
      tierScore = TierScore.A1,
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.UPCOMING,
    )
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertAll(
      { assertThat(snapshot.id).isEqualTo(caseEntity.id) },
      { assertThat(snapshot.tierScore).isNotNull.isEqualTo(caseEntity.tierScore) },
      { assertThat(snapshot.cas1ApplicationId).isNotNull.isEqualTo(caseEntity.cas1ApplicationId) },
      { assertThat(snapshot.cas1ApplicationApplicationStatus).isNotNull.isEqualTo(caseEntity.cas1ApplicationApplicationStatus) },
      { assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isNotNull.isEqualTo(caseEntity.cas1ApplicationRequestForPlacementStatus) },
      { assertThat(snapshot.cas1ApplicationPlacementStatus).isNotNull.isEqualTo(caseEntity.cas1ApplicationPlacementStatus) },
    )
  }

  @Test
  fun `toAggregate maps nullable tier enum fields as null`() {
    val caseEntity = buildCaseEntity(
      tierScore = null,
      cas1ApplicationId = null,
      cas1ApplicationApplicationStatus = null,
      cas1ApplicationRequestForPlacementStatus = null,
      cas1ApplicationPlacementStatus = null,
    )
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.tierScore).isNull()
    assertThat(snapshot.cas1ApplicationId).isNull()
    assertThat(snapshot.cas1ApplicationApplicationStatus).isNull()
    assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isNull()
    assertThat(snapshot.cas1ApplicationPlacementStatus).isNull()
  }

  @Test
  fun `merge updates the entity correctly`() {
    val id = UUID.randomUUID()
    val identifier = UUID.randomUUID().toString()
    val caseEntity = buildCaseEntity(
      id = id,
      tierScore = null,
      cas1ApplicationId = UUID.randomUUID(),
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.ARRIVED,
    ) { withCrn(identifier) }
    val caseAggregate = CaseAggregate.hydrate(
      id,
      tierScore = null,
      cas1ApplicationId = caseEntity.cas1ApplicationId,
      cas1ApplicationApplicationStatus = caseEntity.cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = caseEntity.cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = caseEntity.cas1ApplicationPlacementStatus,
    )

    val identifiersToMerge = mapOf(
      "NEW" to IdentifierType.PRISON_NUMBER,
      identifier to IdentifierType.CRN,
    )

    val newCas1ApplicationId = UUID.randomUUID()
    caseAggregate.upsertCase(
      tierScore = TierScore.A3S,
      cas1ApplicationId = newCas1ApplicationId,
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.WITHDRAWN,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.CANCELLED,
    )

    val mergedEntity = CaseMapper.merge(
      entity = caseEntity,
      snapshot = caseAggregate.snapshot(),
      identifiers = identifiersToMerge,
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(2) },
      {
        assertThat(
          mergedEntity.caseIdentifiers
            .associate { it.identifier to it.identifierType },
        ).isEqualTo(identifiersToMerge)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo(TierScore.A3S) },
      { assertThat(mergedEntity.cas1ApplicationId).isNotNull.isEqualTo(newCas1ApplicationId) },
      { assertThat(mergedEntity.cas1ApplicationApplicationStatus).isNotNull.isEqualTo(Cas1ApplicationStatus.WITHDRAWN) },
      { assertThat(mergedEntity.cas1ApplicationRequestForPlacementStatus).isNotNull.isEqualTo(Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN) },
      { assertThat(mergedEntity.cas1ApplicationPlacementStatus).isNotNull.isEqualTo(Cas1PlacementStatus.CANCELLED) },
    )
  }

  @Test
  fun `merge snapshot into entity only adds missing identifiers`() {
    // set up the case entity with multiple identifiers
    val id = UUID.randomUUID()
    val identifier1 = "CRN1"
    val identifier2 = "CRN2"
    val identifier3 = "PRI1"
    val identifier4 = "PRI2"
    val caseEntity = buildCaseEntity(id = id, tierScore = null) {
      withCrn(identifier1)
      withPrisonNumber(identifier3)
    }

    // add the same identifiers onto the aggregate
    val caseAggregate = CaseAggregate.hydrateNew()

    // crete a set of identifiers containing existing and new
    val identifiersToMerge = mapOf(
      identifier1 to IdentifierType.CRN,
      identifier2 to IdentifierType.CRN,
      identifier3 to IdentifierType.PRISON_NUMBER,
      identifier4 to IdentifierType.PRISON_NUMBER,
    )

    caseAggregate.updateTier(TierScore.A3S)

    val mergedEntity = CaseMapper.merge(
      entity = caseEntity,
      snapshot = caseAggregate.snapshot(),
      identifiers = identifiersToMerge,
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(4) },
      {
        assertThat(
          mergedEntity.caseIdentifiers.associate { it.identifier to it.identifierType },
        ).isEqualTo(identifiersToMerge)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo(TierScore.A3S) },
    )
  }

  @ParameterizedTest
  @EnumSource(TierScore::class)
  fun `toAggregate maps tier enum values correctly`(
    tierScore: TierScore,
  ) {
    val caseEntity = buildCaseEntity(tierScore = tierScore)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.tierScore).isEqualTo(TierScore.valueOf(caseEntity.tierScore!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1ApplicationStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.cas1ApplicationApplicationStatus).isEqualTo(Cas1ApplicationStatus.valueOf(caseEntity.cas1ApplicationApplicationStatus!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1RequestForPlacementStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.cas1ApplicationRequestForPlacementStatus).isEqualTo(Cas1RequestForPlacementStatus.valueOf(caseEntity.cas1ApplicationRequestForPlacementStatus!!.name))
  }

  @ParameterizedTest
  @EnumSource(Cas1PlacementStatus::class)
  fun `toAggregate maps cas1ApplicationApplicationStatus enum values correctly`(
    cas1ApplicationPlacementStatus: Cas1PlacementStatus,
  ) {
    val caseEntity = buildCaseEntity(cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus)
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.cas1ApplicationPlacementStatus).isEqualTo(Cas1PlacementStatus.valueOf(caseEntity.cas1ApplicationPlacementStatus!!.name))
  }

  @Test
  fun `toEntity maps all fields correctly`() {
    val caseAggregate = CaseAggregate.hydrateNew()
    val newCas1ApplicationId = UUID.randomUUID()
    caseAggregate.upsertCase(
      tierScore = TierScore.A3S,
      cas1ApplicationId = newCas1ApplicationId,
      cas1ApplicationApplicationStatus = Cas1ApplicationStatus.WITHDRAWN,
      cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN,
      cas1ApplicationPlacementStatus = Cas1PlacementStatus.CANCELLED,
    )

    val identifier = UUID.randomUUID().toString()
    val identifiersToMerge = mapOf(
      "NEW" to IdentifierType.PRISON_NUMBER,
      identifier to IdentifierType.CRN,
    )

    val mergedEntity = CaseMapper.create(
      snapshot = caseAggregate.snapshot(),
      identifiers = identifiersToMerge,
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(2) },
      {
        assertThat(
          mergedEntity.caseIdentifiers
            .associate { it.identifier to it.identifierType },
        ).isEqualTo(identifiersToMerge)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo(TierScore.A3S) },
      { assertThat(mergedEntity.cas1ApplicationId).isNotNull.isEqualTo(newCas1ApplicationId) },
      { assertThat(mergedEntity.cas1ApplicationApplicationStatus).isNotNull.isEqualTo(Cas1ApplicationStatus.WITHDRAWN) },
      {
        assertThat(mergedEntity.cas1ApplicationRequestForPlacementStatus).isNotNull.isEqualTo(
          Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN,
        )
      },
      { assertThat(mergedEntity.cas1ApplicationPlacementStatus).isNotNull.isEqualTo(Cas1PlacementStatus.CANCELLED) },
    )
  }
}
