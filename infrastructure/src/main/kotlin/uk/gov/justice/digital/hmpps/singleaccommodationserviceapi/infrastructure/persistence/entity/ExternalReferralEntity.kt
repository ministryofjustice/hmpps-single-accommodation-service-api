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
@Table(name = "external_referral")
open class ExternalReferralEntity(
  @Id
  val id: UUID,
  val crn: String,
  val caseId: UUID,
  var referenceNumber: String?,
  var submissionDate: LocalDate,
  @Enumerated(EnumType.STRING)
  var status: ExternalReferralStatus,
  var organisationName: String?,
  var email: String?,
  var phoneNumber: String?,
  var website: String?,
  var submissionNote: String?,
  @Enumerated(EnumType.STRING)
  var outcomeReason: ExternalReferralOutcomeReason?,
  var outcomeNote: String?,

  @DiffIgnore
  @OneToMany(
    mappedBy = "externalReferral",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var notes: MutableList<ExternalReferralNoteEntity> = mutableListOf(),

) : BaseAuditedEntity()

enum class ExternalReferralStatus {
  SUBMITTED,
  ACCEPTED,
  REJECTED,
}

enum class ExternalReferralOutcomeReason {
  ACCEPTED_BY_ORGANISATION,
  ACCEPTED_WITH_ACCOMMODATION_PLACEMENT,
  PERSON_NOT_SUITABLE,
  NO_CAPACITY,
  ANOTHER_REASON,
}
