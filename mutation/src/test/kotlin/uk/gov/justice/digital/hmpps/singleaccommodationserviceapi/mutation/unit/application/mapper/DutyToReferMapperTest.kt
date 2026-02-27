package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrOutcomeStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildDutyToReferSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrOutcomeStatus as EntityDtrOutcomeStatus

class DutyToReferMapperTest {

  @Test
  fun `toEntity maps all fields correctly`() {
    val snapshot = buildDutyToReferSnapshot(outcomeStatus = DtrOutcomeStatus.ACCEPTED)

    val entity = DutyToReferMapper.toEntity(snapshot)

    assertThat(entity.id).isEqualTo(snapshot.id)
    assertThat(entity.crn).isEqualTo(snapshot.crn)
    assertThat(entity.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(entity.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(entity.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(entity.outcomeStatus).isEqualTo(EntityDtrOutcomeStatus.valueOf(snapshot.outcomeStatus!!.name))
    assertThat(entity.outcomeDate).isEqualTo(snapshot.outcomeDate)
  }

  @Test
  fun `toEntity maps nullable enum fields as null`() {
    val entity = DutyToReferMapper.toEntity(
      snapshot = buildDutyToReferSnapshot(
        outcomeStatus = null,
      ),
    )

    assertThat(entity.outcomeStatus).isNull()
  }

  @ParameterizedTest
  @EnumSource(DtrOutcomeStatus::class)
  fun `toEntity maps outcomeStatus enum values correctly`(
    outcomeStatus: DtrOutcomeStatus,
  ) {
    val entity = DutyToReferMapper.toEntity(
      snapshot = buildDutyToReferSnapshot(outcomeStatus = outcomeStatus),
    )
    assertThat(entity.outcomeStatus).isEqualTo(EntityDtrOutcomeStatus.valueOf(outcomeStatus.name))
  }

  @Test
  fun `toDto maps all fields correctly and sets serviceStatus to SUBMITTED`() {
    val snapshot = buildDutyToReferSnapshot()
    val createdBy = "Joe Bloggs"
    val createdAt = Instant.now()

    val dto = DutyToReferMapper.toDto(snapshot, createdBy, createdAt)

    assertThat(dto.crn).isEqualTo(snapshot.crn)
    assertThat(dto.serviceStatus).isEqualTo(DtrServiceStatus.SUBMITTED)
    assertThat(dto.action).isNull()
    assertThat(dto.submission).isNotNull()
    val submission = dto.submission!!
    assertThat(submission.id).isEqualTo(snapshot.id)
    assertThat(submission.localAuthorityAreaId).isEqualTo(snapshot.localAuthorityAreaId)
    assertThat(submission.localAuthorityAreaName).isNull()
    assertThat(submission.referenceNumber).isEqualTo(snapshot.referenceNumber)
    assertThat(submission.submissionDate).isEqualTo(snapshot.submissionDate)
    assertThat(submission.outcomeStatus).isNull()
    assertThat(submission.outcomeDate).isNull()
    assertThat(submission.createdBy).isEqualTo(createdBy)
    assertThat(submission.createdAt).isEqualTo(createdAt)
  }
}
