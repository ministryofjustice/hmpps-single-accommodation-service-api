package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
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
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val createdByUsername: String? = null,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
  val organisationName: String?,
  val website: String?,
  val submissionNote: String?,
)

data class OtherAccommodationReferralCommand(
  val localAuthorityAreaId: UUID,
  val submissionDate: LocalDate,
  val referenceNumber: String?,
  val status: OtherAccommodationReferralStatus,
  val organisationName: String?,
  val website: String?,
  val submissionNote: String?,
)

enum class OtherAccommodationReferralStatus(override val title: String) : TitleEnum {
  SUBMITTED("Submitted"),
}
