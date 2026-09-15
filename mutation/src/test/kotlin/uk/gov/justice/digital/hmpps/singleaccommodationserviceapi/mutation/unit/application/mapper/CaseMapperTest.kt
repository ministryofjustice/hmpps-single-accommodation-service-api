package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.time.LocalDate
import java.util.UUID

class CaseMapperTest {
  private val caseMapper = CaseMapper()

  @Test
  fun `toAggregate maps all fields correctly`() {
    val caseEntity = buildCaseEntity(
      tierScore = "A1",
      hasSyncedCprProposedAccommodation = true,
      firstName = "First",
      lastName = "Last",
      dateOfBirth = LocalDate.of(2000, 12, 3),
      roshLevelCode = "RMRH",
    )
    val caseAggregate = caseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertAll(
      { assertThat(snapshot.id).isEqualTo(caseEntity.id) },
      { assertThat(snapshot.tierScore).isNotNull.isEqualTo(caseEntity.tierScore) },
      { assertThat(snapshot.hasSyncedCprProposedAccommodation).isTrue() },
      { assertThat(snapshot.firstName).isEqualTo(caseEntity.firstName) },
      { assertThat(snapshot.lastName).isEqualTo(caseEntity.lastName) },
      { assertThat(snapshot.dateOfBirth).isEqualTo(caseEntity.dateOfBirth) },
      { assertThat(snapshot.roshLevelCode).isEqualTo(caseEntity.roshLevelCode) },
    )
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `toAggregate maps hasSyncedCprProposedAccommodation correctly`(hasSyncedCprProposedAccommodation: Boolean) {
    val caseEntity = buildCaseEntity(hasSyncedCprProposedAccommodation = hasSyncedCprProposedAccommodation)
    val caseAggregate = caseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.hasSyncedCprProposedAccommodation).isEqualTo(hasSyncedCprProposedAccommodation)
  }

  @Test
  fun `toAggregate maps nullable tier enum fields as null`() {
    val caseEntity = buildCaseEntity(
      tierScore = null,
    )
    val caseAggregate = caseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.tierScore).isNull()
  }

  @Test
  fun `toAggregate maps null roshLevelCode as null`() {
    val caseEntity = buildCaseEntity(roshLevelCode = null)
    val caseAggregate = caseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.roshLevelCode).isNull()
  }

  @Test
  fun `toAggregate maps accommodation fields correctly`() {
    val currentAccommodation = buildAccommodationSummaryDto(crn = "X12345")
    val nextAccommodation = buildAccommodationSummaryDto(crn = "X12345")
    val caseEntity = buildCaseEntity().apply {
      this.currentAccommodation = currentAccommodation
      this.nextAccommodation = nextAccommodation
      this.accommodationStatus = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE
    }

    val snapshot = caseMapper.toAggregate(caseEntity).snapshot()

    assertAll(
      { assertThat(snapshot.currentAccommodation).isEqualTo(currentAccommodation) },
      { assertThat(snapshot.nextAccommodation).isEqualTo(nextAccommodation) },
      { assertThat(snapshot.accommodationStatus).isEqualTo(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE) },
    )
  }

  @Test
  fun `toAggregate maps null accommodation fields as null`() {
    val caseEntity = buildCaseEntity()

    val snapshot = caseMapper.toAggregate(caseEntity).snapshot()

    assertAll(
      { assertThat(snapshot.currentAccommodation).isNull() },
      { assertThat(snapshot.nextAccommodation).isNull() },
      { assertThat(snapshot.accommodationStatus).isNull() },
    )
  }

  @Test
  fun `merge updates the entity correctly`() {
    val id = UUID.randomUUID()
    val identifier = UUID.randomUUID().toString()
    val caseEntity = buildCaseEntity(
      id = id,
      tierScore = null,
      hasSyncedCprProposedAccommodation = false,
    ) { withCrn(identifier) }
    val caseAggregate = CaseAggregate.hydrate(
      id,
      tierScore = null,
      hasSyncedCprProposedAccommodation = caseEntity.hasSyncedCprProposedAccommodation,
    )

    val identifiersToMerge = mapOf(
      "NEW" to IdentifierType.PRISON_NUMBER,
      identifier to IdentifierType.CRN,
    )

    val dateOfBirth = LocalDate.of(1995, 3, 20)
    val currentAccommodation = buildAccommodationSummaryDto(crn = identifier)
    val nextAccommodation = buildAccommodationSummaryDto(crn = identifier)
    caseAggregate.upsertCase(
      tierScore = "A3S",
      firstName = "Updated",
      lastName = "Person",
      dateOfBirth = dateOfBirth,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      accommodationStatus = CaseAccommodationStatus.NO_FIXED_ABODE,
      roshLevelCode = "RMRH",
    )
    caseAggregate.markCaseAsSyncedWithCprProposedAccommodation()

    val mergedEntity = caseMapper.merge(
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
      { assertThat(mergedEntity.tierScore).isEqualTo("A3S") },
      { assertThat(mergedEntity.hasSyncedCprProposedAccommodation).isTrue() },
      { assertThat(mergedEntity.firstName).isEqualTo("Updated") },
      { assertThat(mergedEntity.lastName).isEqualTo("Person") },
      { assertThat(mergedEntity.dateOfBirth).isEqualTo(dateOfBirth) },
      { assertThat(mergedEntity.currentAccommodation).isEqualTo(currentAccommodation) },
      { assertThat(mergedEntity.nextAccommodation).isEqualTo(nextAccommodation) },
      { assertThat(mergedEntity.accommodationStatus).isEqualTo(CaseAccommodationStatus.NO_FIXED_ABODE) },
      { assertThat(mergedEntity.roshLevelCode).isEqualTo("RMRH") },
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

    caseAggregate.updateTier("A3S")

    val mergedEntity = caseMapper.merge(
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
      { assertThat(mergedEntity.tierScore).isEqualTo("A3S") },
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["A1", "A3S"])
  fun `toAggregate maps tier score values correctly`(
    tierScore: String,
  ) {
    val caseEntity = buildCaseEntity(tierScore = tierScore)
    val caseAggregate = caseMapper.toAggregate(caseEntity)
    val snapshot = caseAggregate.snapshot()

    assertThat(snapshot.tierScore).isEqualTo(caseEntity.tierScore)
  }

  @Test
  fun `toEntity maps all fields correctly`() {
    val caseAggregate = CaseAggregate.hydrateNew()
    val dateOfBirth = LocalDate.of(1992, 8, 11)
    val currentAccommodation = buildAccommodationSummaryDto(crn = "X12345")
    val nextAccommodation = buildAccommodationSummaryDto(crn = "X12345")
    caseAggregate.upsertCase(
      tierScore = "A3S",
      firstName = "First",
      lastName = "Last",
      dateOfBirth = dateOfBirth,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      accommodationStatus = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE,
      roshLevelCode = "RVHR",
    )
    caseAggregate.markCaseAsSyncedWithCprProposedAccommodation()

    val crn = UUID.randomUUID().toString()

    val mergedEntity = caseMapper.create(
      snapshot = caseAggregate.snapshot(),
      crn = crn,
      prisonNumber = "NEW",
    )

    assertAll(
      { assertThat(mergedEntity.caseIdentifiers).hasSize(2) },
      {
        val actualIdentifiers = mergedEntity.caseIdentifiers
          .associate { it.identifier to it.identifierType }
        val expectedIdentifiers = mapOf(
          "NEW" to IdentifierType.PRISON_NUMBER,
          crn to IdentifierType.CRN,
        )
        assertThat(actualIdentifiers).isEqualTo(expectedIdentifiers)
      },
      { assertThat(mergedEntity.tierScore).isEqualTo("A3S") },
      { assertThat(mergedEntity.hasSyncedCprProposedAccommodation).isTrue() },
      { assertThat(mergedEntity.firstName).isEqualTo("First") },
      { assertThat(mergedEntity.lastName).isEqualTo("Last") },
      { assertThat(mergedEntity.dateOfBirth).isEqualTo(dateOfBirth) },
      { assertThat(mergedEntity.currentAccommodation).isEqualTo(currentAccommodation) },
      { assertThat(mergedEntity.nextAccommodation).isEqualTo(nextAccommodation) },
      { assertThat(mergedEntity.accommodationStatus).isEqualTo(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE) },
      { assertThat(mergedEntity.roshLevelCode).isEqualTo("RVHR") },
    )
  }
}
