package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "sas_case")
class CaseEntity(

  @Id
  val id: UUID,
  var hasSyncedCprProposedAccommodation: Boolean,
  var tierScore: String? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var dateOfBirth: LocalDate? = null,

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var currentAccommodation: AccommodationSummaryDto? = null,

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var nextAccommodation: AccommodationSummaryDto? = null,

  @Enumerated(EnumType.STRING)
  var accommodationStatus: CaseAccommodationStatus? = null,

  var roshLevelCode: String? = null,

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf(),

) {
  fun latestCrn() = this.caseIdentifiers.filter { it.identifierType == IdentifierType.CRN }.maxBy { it.createdAt }
    .identifier
  fun latestPrisonNumber() = this.caseIdentifiers.filter { it.identifierType == IdentifierType.PRISON_NUMBER }.maxByOrNull { it.createdAt }
    ?.identifier
}
