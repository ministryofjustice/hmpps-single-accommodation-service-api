package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.externalreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.externalreferral.ExternalReferralTransformer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus as EntityExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason as EntityExternalReferralWithdrawalReason

class ExternalReferralTransformerTest {

  @Nested
  inner class ToExternalReferralDto {
    private val createdByName = "Joe Bloggs"
    private val createdByUsername = "JBLOGGS"

    @Test
    fun `should map all fields correctly`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val entity = buildExternalReferralEntity(
        caseId = caseId,
        status = EntityExternalReferralStatus.SUBMITTED,
      )

      val dto = ExternalReferralTransformer.toExternalReferralDto(
        entity,
        crn,
        createdByName,
        createdByUsername,
      )

      assertThat(dto.caseId).isEqualTo(caseId)
      assertThat(dto.crn).isEqualTo(crn)
      assertThat(dto.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
      assertThat(dto.submission).isNotNull()
      assertThat(dto.submission.createdBy).isEqualTo(createdByName)
      assertThat(dto.submission.createdByUsername).isEqualTo(createdByUsername)
    }

    @Test
    fun `should map all fields correctly with user entity`() {
      val caseId = UUID.randomUUID()
      val crn = UUID.randomUUID().toString()
      val user = buildUserEntity(forename = "Joe", surname = "Bloggs", username = "JBLOGGS")
      val entity = buildExternalReferralEntity(
        caseId = caseId,
        status = EntityExternalReferralStatus.SUBMITTED,
      )

      val dto = ExternalReferralTransformer.toExternalReferralDto(
        entity,
        crn,
        user,
      )

      assertThat(dto.caseId).isEqualTo(caseId)
      assertThat(dto.crn).isEqualTo(crn)
      assertThat(dto.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
      assertThat(dto.submission).isNotNull()
      assertThat(dto.submission.createdBy).isEqualTo(user.displayName())
      assertThat(dto.submission.createdByUsername).isEqualTo("JBLOGGS")
    }
  }

  @Nested
  inner class ToSubmission {
    private val createdByName = "Joe Bloggs"
    private val createdByUsername = "JBLOGGS"

    @Test
    fun `should map all fields correctly`() {
      val id = UUID.randomUUID()
      val createdAt = Instant.parse("2026-02-20T10:00:00Z")
      val entity = buildExternalReferralEntity(
        id = id,
        referenceNumber = "REF-001",
        submissionDate = LocalDate.of(2026, 2, 20),
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
        email = "contact@example.com",
        phoneNumber = "01234567890",
        createdAt = createdAt,
      )

      val result = ExternalReferralTransformer.toSubmission(
        entity,
        createdByName,
        createdByUsername,
      )

      assertThat(result.id).isEqualTo(id)
      assertThat(result.referenceNumber).isEqualTo("REF-001")
      assertThat(result.submissionDate).isEqualTo(LocalDate.of(2026, 2, 20))
      assertThat(result.createdBy).isEqualTo(createdByName)
      assertThat(result.createdByUsername).isEqualTo(createdByUsername)
      assertThat(result.createdAt).isEqualTo(createdAt)
      assertThat(result.organisationName).isEqualTo("Organisation name")
      assertThat(result.website).isEqualTo("https://www.charity.org")
      assertThat(result.submissionNote).isEqualTo("A submission note")
      assertThat(result.email).isEqualTo("contact@example.com")
      assertThat(result.phoneNumber).isEqualTo("01234567890")
    }

    @Test
    fun `should map outcomeNote when populated`() {
      val entity = buildExternalReferralEntity(
        withdrawalReason = EntityExternalReferralWithdrawalReason.ACCEPTED_BY_ORGANISATION,
        outcomeNote = "An outcome note",
      )

      val result = ExternalReferralTransformer.toSubmission(
        entity,
        createdByName,
        createdByUsername,
      )

      assertThat(result.outcomeNote).isEqualTo("An outcome note")
      assertThat(ExternalReferralTransformer.toSubmission(entity, buildUserEntity()).outcomeNote).isEqualTo("An outcome note")
    }

    @Test
    fun `should handle nullable fields correctly`() {
      val entity = buildExternalReferralEntity(
        referenceNumber = null,
        organisationName = null,
        website = null,
        submissionNote = null,
        email = null,
        phoneNumber = null,
      )

      val result = ExternalReferralTransformer.toSubmission(
        entity,
        createdByName,
        createdByUsername,
      )

      assertThat(result.referenceNumber).isNull()
      assertThat(result.organisationName).isNull()
      assertThat(result.website).isNull()
      assertThat(result.submissionNote).isNull()
      assertThat(result.email).isNull()
      assertThat(result.phoneNumber).isNull()
      assertThat(result.createdByUsername).isEqualTo(createdByUsername)
      assertThat(result.outcomeNote).isNull()
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(EntityExternalReferralStatus::class)
    fun `should map all ExternalReferralStatus values correctly`(entityStatus: EntityExternalReferralStatus) {
      val result = ExternalReferralTransformer.toStatus(entityStatus)
      assertThat(result.name).isEqualTo(entityStatus.name)
    }

    @ParameterizedTest
    @EnumSource(EntityExternalReferralWithdrawalReason::class)
    fun `should map all ExternalReferralWithdrawalReason values correctly`(entityWithdrawalReason: EntityExternalReferralWithdrawalReason) {
      val result = ExternalReferralTransformer.toWithdrawalReason(entityWithdrawalReason)
      assertThat(result?.name).isEqualTo(entityWithdrawalReason.name)
    }

    @Test
    fun `should map null withdrawalReason to null`() {
      assertThat(ExternalReferralTransformer.toWithdrawalReason(null)).isNull()
    }
  }
}
