package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrOutcomeStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferSnapshot(
  id: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  outcomeStatus: DtrOutcomeStatus? = null,
  outcomeDate: LocalDate? = null,
) = DutyToReferSnapshot(
  id = id,
  crn = crn,
  localAuthorityAreaId = localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  outcomeStatus = outcomeStatus,
  outcomeDate = outcomeDate,
)
