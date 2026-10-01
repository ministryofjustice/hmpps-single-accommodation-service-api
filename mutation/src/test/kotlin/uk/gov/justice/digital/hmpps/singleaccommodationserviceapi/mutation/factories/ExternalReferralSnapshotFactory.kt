package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate.ExternalReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate.ExternalReferralSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildExternalReferralSnapshot(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  referenceNumber: String? = "REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 2, 20),
  status: ExternalReferralStatus = ExternalReferralStatus.SUBMITTED,
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = "A submission note",
  email: String? = null,
  phoneNumber: String? = null,
  outcomeReason: ExternalReferralOutcomeReason? = null,
  outcomeNote: String? = null,
  notes: List<ExternalReferralNote> = emptyList(),
) = ExternalReferralSnapshot(
  id = id,
  caseId = caseId,
  crn = crn,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  email = email,
  phoneNumber = phoneNumber,
  outcomeReason = outcomeReason,
  outcomeNote = outcomeNote,
  notes = notes,
)

fun buildExternalReferralNote(id: UUID = UUID.randomUUID(), note: String = "Test note") = ExternalReferralNote(id, note)
