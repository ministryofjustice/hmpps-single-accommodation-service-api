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
import org.javers.core.metamodel.annotation.DiffIgnore
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation")
open class ProposedAccommodationEntity(
  @Id
  val id: UUID,
  val caseId: UUID,
  var name: String?,
  @Column(name = "accommodation_type_id", nullable = false)
  var accommodationTypeId: UUID,
  @DiffIgnore
  @Column(name = "accommodation_status_id", nullable = true)
  var accommodationStatusId: UUID?,
  @Enumerated(EnumType.STRING)
  var verificationStatus: VerificationStatus?,
  @Enumerated(EnumType.STRING)
  var nextAccommodationStatus: NextAccommodationStatus?,
  var startDate: LocalDate?,
  var endDate: LocalDate?,
  var postcode: String?,
  var subBuildingName: String?,
  var buildingName: String?,
  var buildingNumber: String?,
  var throughfareName: String?,
  var dependentLocality: String?,
  var postTown: String?,
  var county: String?,
  var country: String?,
  var uprn: String?,

  @DiffIgnore
  @OneToMany(
    mappedBy = "proposedAccommodation",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var notes: MutableList<ProposedAccommodationNoteEntity> = mutableListOf(),

) : BaseAuditedEntity()

enum class VerificationStatus {
  NOT_CHECKED_YET,
  FAILED,
  PASSED,
}

enum class NextAccommodationStatus {
  YES,
  NO,
  TO_BE_DECIDED,
}
