package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.proposedaccommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationTransformerTest {

  @Nested
  inner class ToAccommodationDetail {
    private val createdBy = "Joe Bloggs"

    @Test
    fun `should transform entity to AccommodationDetail with all fields`() {
      val id = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val createdAt = Instant.parse("2024-01-15T10:00:00Z")
      val startDate = LocalDate.of(2024, 2, 1)
      val endDate = LocalDate.of(2024, 6, 30)
      val accommodationTypeEntity = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Living in the home of a friend, family member or partner: settled",
      )
      val accommodationStatusEntity = buildAccommodationStatusEntity(
        code = "M",
        name = "Main",
      )
      val entity = buildProposedAccommodationEntity(
        id = id,
        name = "Test Name",
        accommodationTypeEntity = accommodationTypeEntity,
        verificationStatus = EntityVerificationStatus.PASSED,
        nextAccommodationStatus = EntityNextAccommodationStatus.YES,
        startDate = startDate,
        endDate = endDate,
        postcode = "SW1A 1AA",
        subBuildingName = "Flat 1",
        buildingName = "Test Building",
        buildingNumber = "10",
        throughfareName = "Test Street",
        dependentLocality = "Test Locality",
        postTown = "London",
        county = "Greater London",
        country = "England",
        uprn = "12345678",
        createdByUserId = UUID.randomUUID(),
        createdAt = createdAt,
      )

      val result = ProposedAccommodationTransformer.toAccommodationDetail(entity, accommodationTypeEntity, accommodationStatusEntity, crn, createdBy)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.name).isEqualTo("Test Name")
      assertThat(result.accommodationType?.code).isEqualTo(accommodationTypeEntity.code)
      assertThat(result.accommodationType?.description).isEqualTo(accommodationTypeEntity.name)
      assertThat(result.accommodationStatus?.code).isEqualTo(accommodationStatusEntity.code)
      assertThat(result.accommodationStatus?.description).isEqualTo(accommodationStatusEntity.name)
      assertThat(result.verificationStatus).isEqualTo(VerificationStatus.PASSED)
      assertThat(result.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
      assertThat(result.startDate).isEqualTo(startDate)
      assertThat(result.endDate).isEqualTo(endDate)
      assertThat(result.createdBy).isEqualTo(createdBy)
      assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should handle nullable fields correctly`() {
      val crn = UUID.randomUUID().toString()
      val entity = buildProposedAccommodationEntity(
        name = null,
        verificationStatus = null,
        nextAccommodationStatus = null,
        startDate = null,
        endDate = null,
        postcode = null,
        subBuildingName = null,
        buildingName = null,
        buildingNumber = null,
        throughfareName = null,
        dependentLocality = null,
        postTown = null,
        county = null,
        country = null,
        uprn = null,
      )

      val result = ProposedAccommodationTransformer.toAccommodationDetail(entity, accommodationTypeEntity = null, null, crn, createdBy)

      assertThat(result.name).isNull()
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.verificationStatus).isNull()
      assertThat(result.nextAccommodationStatus).isNull()
      assertThat(result.accommodationType).isNull()
      assertThat(result.accommodationStatus).isNull()
      assertThat(result.startDate).isNull()
      assertThat(result.endDate).isNull()
      assertThat(result.address.postcode).isNull()
      assertThat(result.address.subBuildingName).isNull()
      assertThat(result.address.buildingName).isNull()
      assertThat(result.address.buildingNumber).isNull()
      assertThat(result.address.thoroughfareName).isNull()
      assertThat(result.address.dependentLocality).isNull()
      assertThat(result.address.postTown).isNull()
      assertThat(result.address.county).isNull()
      assertThat(result.address.country).isNull()
      assertThat(result.address.uprn).isNull()
    }
  }

  @Nested
  inner class ToAddressDetails {

    @Test
    fun `should reconstruct address from flattened entity fields`() {
      val entity = buildProposedAccommodationEntity(
        postcode = "RG26 5AG",
        subBuildingName = "Sub Building",
        buildingName = "Building Name",
        buildingNumber = "42",
        throughfareName = "Main Street",
        dependentLocality = "Village",
        postTown = "Town",
        county = "County",
        country = "Country",
        uprn = "UP123",
      )

      val result = ProposedAccommodationTransformer.toAddressDetails(entity)

      assertThat(result.postcode).isEqualTo("RG26 5AG")
      assertThat(result.subBuildingName).isEqualTo("Sub Building")
      assertThat(result.buildingName).isEqualTo("Building Name")
      assertThat(result.buildingNumber).isEqualTo("42")
      assertThat(result.thoroughfareName).isEqualTo("Main Street")
      assertThat(result.dependentLocality).isEqualTo("Village")
      assertThat(result.postTown).isEqualTo("Town")
      assertThat(result.county).isEqualTo("County")
      assertThat(result.country).isEqualTo("Country")
      assertThat(result.uprn).isEqualTo("UP123")
    }

    @Test
    fun `should map entity throughfareName typo to DTO thoroughfareName`() {
      val entity = buildProposedAccommodationEntity(
        throughfareName = "Test Thoroughfare",
      )

      val result = ProposedAccommodationTransformer.toAddressDetails(entity)

      assertThat(result.thoroughfareName).isEqualTo("Test Thoroughfare")
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(EntityVerificationStatus::class)
    fun `should map all VerificationStatus values correctly`(entityStatus: EntityVerificationStatus) {
      val result = ProposedAccommodationTransformer.toVerificationStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }

    @ParameterizedTest
    @EnumSource(EntityNextAccommodationStatus::class)
    fun `should map all NextAccommodationStatus values correctly`(entityStatus: EntityNextAccommodationStatus) {
      val result = ProposedAccommodationTransformer.toNextAccommodationStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }
  }
}
