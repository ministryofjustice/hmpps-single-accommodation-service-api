package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation_note")
data class ProposedAccommodationNoteEntity(
  @Id
  val id: UUID,
  var note: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "proposed_accommodation_id")
  var proposedAccommodation: ProposedAccommodationEntity,
) : BaseAuditedEntity()
