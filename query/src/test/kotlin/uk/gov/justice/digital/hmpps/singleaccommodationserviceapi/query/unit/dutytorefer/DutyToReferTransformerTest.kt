package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.dutytorefer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferTransformer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class DutyToReferTransformerTest {

  @Nested
  inner class ToNotStartedDto {

    @Test
    fun `should return NOT_STARTED status with null submission`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val dto = DutyToReferTransformer.toNotStartedDto(caseId = caseId, crn = crn)

      assertThat(dto.crn).isEqualTo(crn)
      assertThat(dto.caseId).isEqualTo(caseId)
      assertThat(dto.status).isEqualTo(DtrStatus.NOT_STARTED)
      assertThat(dto.submission).isNull()
    }
  }

  @Nested
  inner class ToDutyToReferDto {
    private val createdByName = "Joe Bloggs"

    @Test
    fun `should map all fields correctly`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val entity = buildDutyToReferEntity(
        caseId = caseId,
        status = EntityDtrStatus.SUBMITTED,
      )

      val dto = DutyToReferTransformer.toDutyToReferDto(entity, crn, createdByName, "Test Local Authority")

      assertThat(dto.caseId).isEqualTo(caseId)
      assertThat(dto.crn).isEqualTo(crn)
      assertThat(dto.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(dto.submission).isNotNull()
    }
  }

  @Nested
  inner class ToSubmission {
    private val createdByName = "Joe Bloggs"

    @Test
    fun `should map all fields correctly`() {
      val id = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()
      val createdAt = Instant.parse("2026-01-15T10:00:00Z")
      val entity = buildDutyToReferEntity(
        id = id,
        localAuthorityAreaId = localAuthorityAreaId,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        createdAt = createdAt,
      )
      val localAuthorityAreaName = "Test Local Authority"

      val result = DutyToReferTransformer.toSubmission(entity, createdByName, localAuthorityAreaName)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(result.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaName)
      assertThat(result.referenceNumber).isEqualTo("DTR-REF-001")
      assertThat(result.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
      assertThat(result.createdBy).isEqualTo(createdByName)
      assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should handle nullable fields correctly`() {
      val entity = buildDutyToReferEntity(referenceNumber = null)

      val result = DutyToReferTransformer.toSubmission(entity, createdByName, null)

      assertThat(result.localAuthority.localAuthorityAreaName).isNull()
      assertThat(result.referenceNumber).isNull()
    }
  }

  @Nested
  inner class ToLocalAuthority {

    @Test
    fun `should map all fields correctly`() {
      val localAuthorityAreaId = UUID.randomUUID()
      val entity = buildDutyToReferEntity(localAuthorityAreaId = localAuthorityAreaId)
      val localAuthorityAreaName = "Test Local Authority"

      val result = DutyToReferTransformer.toLocalAuthority(entity, localAuthorityAreaName)

      assertThat(result.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(result.localAuthorityAreaName).isEqualTo(localAuthorityAreaName)
    }

    @Test
    fun `should handle null name`() {
      val entity = buildDutyToReferEntity()

      val result = DutyToReferTransformer.toLocalAuthority(entity, null)

      assertThat(result.localAuthorityAreaId).isEqualTo(entity.localAuthorityAreaId)
      assertThat(result.localAuthorityAreaName).isNull()
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(EntityDtrStatus::class)
    fun `should map all DtrStatus values correctly`(entityStatus: EntityDtrStatus) {
      val result = DutyToReferTransformer.toStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }
  }
}
