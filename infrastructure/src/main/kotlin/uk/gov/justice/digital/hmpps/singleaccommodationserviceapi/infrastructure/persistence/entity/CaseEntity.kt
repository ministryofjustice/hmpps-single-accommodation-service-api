package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

// TODO do we persist cs data?
@Entity
@Table(name = "sas_case")
data class CaseEntity(
  @Id
  val id: UUID,
  val crn: String, // cs

  // Tier event data
  val tier: TierScore?, // tier

  // NDelius Data
//  val staffCode: String?, // cs
//  val staffName: String?, // cs
//  val teamCode: String?, // cs
//  val teamDescription: String?, // cs

  // OASys
//  val riskLevel: RiskLevel?, // cs

  // CPR data
  val currentAccommodationArrangementType: AccommodationArrangementType?, // ???
  val nextAccommodationId: UUID?, // ???
//  val sexCode: SexCode?, // cs
//  val nomsNumber: String?, // cs
//  val pncNumber: String?, // cs
//  val dateOfBirth: LocalDate?, // cs
//  val name: String?, // cs

  // Prisoner Search Data
//  val releaseDate: LocalDate?, // cs

  val crsStatus: String?, // ???

  val dtrStatus: String?, // SAS

  // CAS Application data
  val cas3ApplicationPlacementStatus: Cas3PlacementStatus?, // cas
  val cas3ApplicationApplicationStatus: Cas3ApplicationStatus?, // cas
  val cas3ApplicationId: UUID?, // cas

  val cas2HdcApplicationId: UUID?, // cas

  val cas2PrisonBailApplicationId: UUID?, // cas

  val cas2CourtBailApplicationId: UUID?, // cas

  val cas1ApplicationPlacementStatus: Cas1PlacementStatus?, // cas
  val cas1ApplicationApplicationStatus: Cas1ApplicationStatus?, // cas
  val cas1ApplicationId: UUID?, // cas
)
