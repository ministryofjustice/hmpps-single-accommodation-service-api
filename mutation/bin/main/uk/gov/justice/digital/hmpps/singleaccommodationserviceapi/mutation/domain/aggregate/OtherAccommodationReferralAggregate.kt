package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import java.time.LocalDate
import java.util.UUID

class OtherAccommodationReferralAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private val crn: String,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: OtherAccommodationReferralStatus? = null,
  private var organisationName: String? = null,
  private var website: String? = null,
  private var submissionNote: String? = null,
) {
  companion object {
    fun hydrateNew(caseId: UUID, crn: String) = OtherAccommodationReferralAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
      crn = crn,
    )
  }

  fun updateOtherAccommodationReferral(
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: OtherAccommodationReferralStatus,
    organisationName: String?,
    website: String?,
    submissionNote: String?,
  ) {
    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.organisationName = organisationName
    this.website = website
    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }
  }

  fun snapshot() = OtherAccommodationReferralSnapshot(
    id = id,
    caseId = caseId,
    crn = crn,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    organisationName = organisationName,
    website = website,
    submissionNote = submissionNote,
  )

  data class OtherAccommodationReferralSnapshot(
    val id: UUID,
    val caseId: UUID,
    val crn: String,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: OtherAccommodationReferralStatus,
    val organisationName: String? = null,
    val website: String? = null,
    val submissionNote: String? = null,
  )
}
