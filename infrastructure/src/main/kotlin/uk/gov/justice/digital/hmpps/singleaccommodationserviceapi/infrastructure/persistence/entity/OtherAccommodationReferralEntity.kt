package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.javers.core.metamodel.annotation.DiffIgnore
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "other_accommodation_referral")
open class OtherAccommodationReferralEntity(
  @Id
  val id: UUID,
  val caseId: UUID,
  var referenceNumber: String?,
  var submissionDate: LocalDate,
  @Enumerated(EnumType.STRING)
  var status: OorStatus,
  @Enumerated(EnumType.STRING)
  var outcomeReason: OutcomeReason? = null,
  var submissionNote: String? = null,
  var outcomeNote: String? = null,

  @DiffIgnore
  @OneToMany(
    mappedBy = "otherAccommodationReferral",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var notes: MutableList<OtherAccommodationReferralNoteEntity> = mutableListOf(),

) : BaseAuditedEntity()

enum class OorStatus {
  SUBMITTED,
  ACCEPTED,
  NOT_ACCEPTED,
}
