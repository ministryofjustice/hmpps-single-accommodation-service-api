package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.outofregionreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.outofregionreferral.OutOfRegionReferralTransformer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus

class OutOfRegionReferralTransformerTest {

  @Nested
  inner class ToOutOfRegionReferralDto {
    private val createdByName = "Joe Bloggs"

    @Test
    fun `should map all fields correctly`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val entity = buildOutOfRegionReferralEntity(
        caseId = caseId,
        status = EntityOorStatus.SUBMITTED,
      )

      val dto = OutOfRegionReferralTransformer.toOutOfRegionReferralDto(entity, crn, createdByName)

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
      val entity = buildOutOfRegionReferralEntity(
        id = id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        createdAt = createdAt,
      )

      val result = OutOfRegionReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.referenceNumber).isEqualTo("OOR-REF-001")
      assertThat(result.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
      assertThat(result.createdBy).isEqualTo(createdByName)
      assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should handle nullable fields correctly`() {
      val entity = buildOutOfRegionReferralEntity(referenceNumber = null)

      val result = OutOfRegionReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.referenceNumber).isNull()
      assertThat(result.submissionNote).isNull()
      assertThat(result.outcomeNote).isNull()
    }

    @Test
    fun `should map submissionNote and outcomeNote correctly`() {
      val entity = buildOutOfRegionReferralEntity(
        submissionNote = "A submission note",
        outcomeNote = "An outcome note",
      )

      val result = OutOfRegionReferralTransformer.toSubmission(entity, createdByName)

      assertThat(result.submissionNote).isEqualTo("A submission note")
      assertThat(result.outcomeNote).isEqualTo("An outcome note")
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(EntityOorStatus::class)
    fun `should map all OorStatus values correctly`(entityStatus: EntityOorStatus) {
      val result = OutOfRegionReferralTransformer.toStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }
  }
}
