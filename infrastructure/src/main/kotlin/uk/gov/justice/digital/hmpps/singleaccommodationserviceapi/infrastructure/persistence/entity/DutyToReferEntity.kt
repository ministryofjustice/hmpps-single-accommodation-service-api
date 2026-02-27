package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "duty_to_refer")
open class DutyToReferEntity(
  @Id
  val id: UUID,
  val crn: String,
  val localAuthorityAreaId: UUID,
  var referenceNumber: String?,
  var submissionDate: LocalDate,
  @Enumerated(EnumType.STRING)
  var outcomeStatus: DtrOutcomeStatus?,
  var outcomeDate: LocalDate?,
) : BaseAuditedEntity()

enum class DtrOutcomeStatus {
  ACCEPTED,
  NOT_ACCEPTED,
}
