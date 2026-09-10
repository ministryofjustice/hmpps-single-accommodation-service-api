package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate.OtherAccommodationReferralSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

object OtherAccommodationReferralMapper {

  fun toEntity(snapshot: OtherAccommodationReferralSnapshot) = OtherAccommodationReferralEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    caseId = snapshot.caseId,
    localAuthorityAreaId = snapshot.localAuthorityAreaId,
    referenceNumber = snapshot.referenceNumber,
    submissionDate = snapshot.submissionDate,
    status = EntityOtherAccommodationReferralStatus.valueOf(snapshot.status.name),
    organisationName = snapshot.organisationName,
    website = snapshot.website,
    submissionNote = snapshot.submissionNote,
  )

  fun toDto(
    snapshot: OtherAccommodationReferralSnapshot,
    createdBy: String,
    createdByUsername: String?,
    createdAt: Instant,
    localAuthorityAreaName: String?,
  ) = OtherAccommodationReferralDto(
    caseId = snapshot.caseId,
    crn = snapshot.crn,
    status = OtherAccommodationReferralStatus.valueOf(snapshot.status.name),
    submission = OtherAccommodationReferralSubmissionDto(
      id = snapshot.id,
      localAuthority = LocalAuthorityDto(
        localAuthorityAreaId = snapshot.localAuthorityAreaId,
        localAuthorityAreaName = localAuthorityAreaName,
      ),
      referenceNumber = snapshot.referenceNumber,
      submissionDate = snapshot.submissionDate,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = createdAt,
      organisationName = snapshot.organisationName,
      website = snapshot.website,
      submissionNote = snapshot.submissionNote,
    ),
  )
}
