package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.otheraccommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral.OtherAccommodationReferralTransformer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus

class OtherAccommodationReferralTransformerTest {

  @Nested
  inner class ToOtherAccommodationReferralDto {
    private val createdByName = "Joe Bloggs"

    @Test
    fun `should map all fields correctly`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val entity = buildOtherAccommodationReferralEntity(
        caseId = caseId,
        status = EntityOorStatus.SUBMITTED,
      )

      val dto = OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(entity, crn, createdByName)

      assertThat(dto.caseId).isEqualTo(caseId)
      assertThat(dto.crn).isEqualTo(crn)
      assertThat(dto.status).isEqualTo(OorStatus.SUBMITTED)
      assertThat(dto.submission).isNotNull()
    }
  }

  @Nested
  inner class ToSubmission {
    private val createdByName = "Joe Bloggs"

    @Test
    fun `should map all fields correctly`() {
      val id = UUID.randomUUID()
      val createdAt = Instant.parse("2026-01-15T10:00:00Z")
      val entity = buildOtherAccommodationReferralEntity(
        id = id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        createdAt = createdAt,
      )

      val result = OtherAccommodationReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.referenceNumber).isEqualTo("OOR-REF-001")
      assertThat(result.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
      assertThat(result.createdBy).isEqualTo(createdByName)
      assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should handle nullable fields correctly`() {
      val entity = buildOtherAccommodationReferralEntity(referenceNumber = null)

      val result = OtherAccommodationReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.referenceNumber).isNull()
      assertThat(result.submissionNote).isNull()
      assertThat(result.outcomeNote).isNull()
    }

    @Test
    fun `should map submissionNote and outcomeNote correctly`() {
      val entity = buildOtherAccommodationReferralEntity(
        submissionNote = "A submission note",
        outcomeNote = "An outcome note",
      )

      val result = OtherAccommodationReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.submissionNote).isEqualTo("A submission note")
      assertThat(result.outcomeNote).isEqualTo("An outcome note")
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(EntityOorStatus::class)
    fun `should map all OorStatus values correctly`(entityStatus: EntityOorStatus) {
      val result = OtherAccommodationReferralTransformer.toStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }
  }
}
