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
@Table(name = "duty_to_refer")
open class DutyToReferEntity(
  @Id
  val id: UUID,
  val caseId: UUID,
  var localAuthorityAreaId: UUID,
  var referenceNumber: String?,
  var submissionDate: LocalDate,
  @Enumerated(EnumType.STRING)
  var status: DtrStatus,
  @Enumerated(EnumType.STRING)
  var withdrawalReason: WithdrawalReason? = null,
  var withdrawalReasonOther: String? = null,
  @Enumerated(EnumType.STRING)
  var outcomeReason: OutcomeReason? = null,
  var submissionNote: String? = null,
  var outcomeNote: String? = null,

  @DiffIgnore
  @OneToMany(
    mappedBy = "dutyToRefer",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var notes: MutableList<DutyToReferNoteEntity> = mutableListOf(),

) : BaseAuditedEntity()

enum class DtrStatus {
  SUBMITTED,
  ACCEPTED,
  NOT_ACCEPTED,
  WITHDRAWN,
}

enum class WithdrawalReason {
  NEW_REFERRAL,
  INCORRECT_LOCAL_AUTHORITY,
  NO_CONSENT,
  DISENGAGED,
  HOUSING_NEED_RESOLVED,
  NOT_ELIGIBLE,
  OTHER,
}

enum class OutcomeReason {
  PREVENTION_AND_RELIEF_DUTY,
  PRIORITY_NEED,
  NO_LOCAL_CONNECTION,
  INTENTIONALLY_HOMELESS,
  REJECTED_FOR_ANOTHER_REASON,
}
