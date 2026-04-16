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
}
