package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class OtherAccommodationReferralDto(
  val caseId: UUID,
  val crn: String,
  val status: OtherAccommodationReferralStatus,
  val submission: OtherAccommodationReferralSubmissionDto,
)

data class OtherAccommodationReferralSubmissionDto(
  val id: UUID,
  val localAuthority: LocalAuthorityDto,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val createdBy: String,
  val createdByUsername: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
  val organisationName: String?,
  val website: String?,
  val submissionNote: String?,
  val outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
  val outcomeNote: String? = null,
)

data class OtherAccommodationReferralCommand(
  val localAuthorityAreaId: UUID,
  val submissionDate: LocalDate,
  val referenceNumber: String?,
  val status: OtherAccommodationReferralStatus,
  val organisationName: String?,
  val website: String?,
  val submissionNote: String?,
  val outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
  val outcomeNote: String? = null,
)

enum class OtherAccommodationReferralStatus(override val title: String) : TitleEnum {
  SUBMITTED("Submitted"),
  ACCEPTED("Accepted"),
  REJECTED("Rejected"),
}

enum class OtherAccommodationReferralOutcomeReason {
  ACCEPTED_BY_ORGANISATION,
  ACCEPTED_WITH_ACCOMMODATION_PLACEMENT,
  PERSON_NOT_SUITABLE,
  NO_CAPACITY,
  ANOTHER_REASON,
}
