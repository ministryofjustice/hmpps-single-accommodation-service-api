package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "other_accommodation_referral")
open class OtherAccommodationReferralEntity(
  @Id
  val id: UUID,
  val crn: String,
  val caseId: UUID,
  var localAuthorityAreaId: UUID,
  var referenceNumber: String?,
  var submissionDate: LocalDate,
  @Enumerated(EnumType.STRING)
  var status: OtherAccommodationReferralStatus,
  var organisationName: String?,
  var website: String?,
  var submissionNote: String?,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
  var createdByUser: UserEntity? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_updated_by_user_id", insertable = false, updatable = false)
  var lastUpdatedByUser: UserEntity? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "local_authority_area_id", insertable = false, updatable = false)
  var localAuthorityArea: LocalAuthorityAreaEntity? = null,

  @OneToMany(
    mappedBy = "otherAccommodationReferral",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var notes: MutableList<OtherAccommodationReferralNoteEntity> = mutableListOf(),

) : BaseAuditedEntity()

enum class OtherAccommodationReferralStatus {
  SUBMITTED,
}
