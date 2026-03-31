package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferSnapshot(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: DtrStatus = DtrStatus.SUBMITTED,
) = DutyToReferSnapshot(
  id = id,
  caseId = caseId,
  localAuthorityAreaId = localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
)
