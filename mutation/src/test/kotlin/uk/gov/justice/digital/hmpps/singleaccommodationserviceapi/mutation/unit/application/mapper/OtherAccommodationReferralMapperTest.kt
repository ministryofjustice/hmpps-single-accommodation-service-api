package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildOtherAccommodationReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

class OtherAccommodationReferralMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot()

    val entity = OtherAccommodationReferralMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.caseId).isEqualTo(snapshot.caseId)
    assertThat(entity.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name))
    assertThat(entity.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(entity.website).isEqualTo(snapshot.website)
    assertThat(entity.submissionNote).isEqualTo(snapshot.submissionNote)
  }

  @Test
  fun `toDto maps all fields correctly`() {
    val snapshot = buildOtherAccommodationReferralSnapshot(status = OtherAccommodationReferralStatus.SUBMITTED)
    val createdBy = "Joe Bloggs"
    val createdByUsername = "joe.bloggs"
    val createdAt = Instant.now()
    val localAuthorityAreaName = "Test Local Authority"

    val dto = OtherAccommodationReferralMapper.toDto(
      snapshot = snapshot,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
      localAuthorityAreaName = localAuthorityAreaName,
    )

    assertThat(dto.caseId).isEqualTo(snapshot.caseId)
    assertThat(dto.crn).isEqualTo(snapshot.crn)
    assertThat(dto.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
    assertThat(dto.submission.id).isEqualTo(snapshot.id)
    assertThat(dto.submission.localAuthority.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(dto.submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaName)
    assertThat(dto.submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(dto.submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(dto.submission.createdBy).isEqualTo(createdBy)
    assertThat(dto.submission.createdByUsername).isEqualTo(createdByUsername)
    assertThat(dto.submission.createdAt).isEqualTo(createdAt)
    assertThat(dto.submission.organisationName).isEqualTo(snapshot.organisationName)
    assertThat(dto.submission.website).isEqualTo(snapshot.website)
    assertThat(dto.submission.submissionNote).isEqualTo(snapshot.submissionNote)
  }
}
