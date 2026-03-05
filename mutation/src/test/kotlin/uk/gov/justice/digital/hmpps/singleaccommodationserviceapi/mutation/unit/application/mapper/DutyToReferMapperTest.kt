package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class DutyToReferMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildDutyToReferSnapshot()

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.status).isEqualTo(EntityDtrStatus.valueOf(snapshot.status.name))
  }

  @ParameterizedTest
  @EnumSource(value = DtrStatus::class, names = ["SUBMITTED", "ACCEPTED", "NOT_ACCEPTED"])
  fun `toEntity maps status enum values correctly`(
    status: DtrStatus,
  ) {
    val entity = DutyToReferMapper.toEntity(
      snapshot = buildDutyToReferSnapshot(status = status),
    )
    assertThat(entity.status).isEqualTo(EntityDtrStatus.valueOf(status.name))
  }

  @Test
  fun `toDto maps all fields correctly and sets status from snapshot`() {
    val snapshot = buildDutyToReferSnapshot(status = DtrStatus.ACCEPTED)
    val createdBy = "Joe Bloggs"
    val createdAt = Instant.now()

    val dto = DutyToReferMapper.toDto(snapshot, createdBy, createdAt)

    assertThat(dto.crn).isEqualTo(snapshot.crn)
    assertThat(dto.status).isEqualTo(DtrStatus.ACCEPTED)
    assertThat(dto.submission).isNotNull()
    val submission = dto.submission!!
    assertThat(submission.id).isEqualTo(snapshot.id)
    assertThat(submission.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(submission.createdBy).isEqualTo(createdBy)
    assertThat(submission.createdAt).isEqualTo(createdAt)
  }
}
