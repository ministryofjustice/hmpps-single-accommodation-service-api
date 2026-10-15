package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sas_case_identifier")
@SQLRestriction("archived = false")
class CaseIdentifierEntity(

  @Id
  val id: UUID = UUID.randomUUID(),

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "case_id", nullable = false)
  var caseEntity: CaseEntity,
  val identifier: String,
  @Enumerated(EnumType.STRING)
  val identifierType: IdentifierType,
  val createdAt: Instant = Instant.now(),
  var archived: Boolean = false,
)

enum class IdentifierType {
  CRN,
  PRISON_NUMBER,
}
