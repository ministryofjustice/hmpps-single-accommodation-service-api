package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseListItem
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.StaffName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationOrchestrationDto
import java.time.LocalDate
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private var tier: TierScore? = null,
  private var cas1ApplicationId: UUID? = null,
  private var cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  private var cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
  private var cas2CourtBailApplicationId: UUID? = null,
  private var cas2PrisonBailApplicationId: UUID? = null,
  private var cas2HdcApplicationId: UUID? = null,
  private var cas3ApplicationId: UUID? = null,
  private var cas3ApplicationApplicationStatus: Cas3ApplicationStatus? = null,
  private var cas3ApplicationPlacementStatus: Cas3PlacementStatus? = null,
  private var dtrStatus: String? = null,
  private var crsStatus: String? = null,
  private var releaseDate: LocalDate? = null,
  private var name: String? = null,
  private var dateOfBirth: LocalDate? = null,
  private var pncNumber: String? = null,
  private var nomsNumber: String? = null,
  private var sexCode: SexCode? = null,
  private var nextAccommodationId: UUID? = null,
  private var currentAccommodationArrangementType: AccommodationArrangementType? = null,
  private var riskLevel: RiskLevel? = null,
  private var teamDescription: String? = null,
  private var teamCode: String? = null,
  private var staffName: String? = null,
  private var staffCode: String? = null,
) {

  fun upsertTier(
    newTier: TierScore,
  ) {
    tier = newTier
  }

  fun upsertCase(
    newCaseListItem: CaseListItem,
    freshCase: CaseApplicationOrchestrationDto,
    prisoner: Prisoner?,
  ) {
    releaseDate = prisoner?.releaseDate ?: newCaseListItem.expectedReleaseDate
    name = freshCase.cpr?.let { toCprFullName(it) } ?: toFullName(newCaseListItem.name)
    dateOfBirth = freshCase.cpr?.dateOfBirth ?: newCaseListItem.dateOfBirth
    pncNumber = freshCase.cpr?.let { it.identifiers?.pncs?.first() } ?: newCaseListItem.pncNumber
    nomsNumber = freshCase.cpr?.let { it.identifiers?.prisonNumbers?.first() } ?: newCaseListItem.nomsNumber
    // TODO map gender
    sexCode = freshCase.cpr?.let { it.sex?.code } ?: SexCode.valueOf(newCaseListItem.gender)
    // TODO map roshLevel
    riskLevel = RiskLevel.VERY_HIGH
    teamDescription = newCaseListItem.team.description
    teamCode = newCaseListItem.team.code
    staffName = toStaffFullName(newCaseListItem.staff.name)
    staffCode = newCaseListItem.staff.code
    tier = freshCase.tier?.tierScore
    cas1ApplicationId = freshCase.cas1Application?.id
    cas1ApplicationApplicationStatus = freshCase.cas1Application?.applicationStatus
    cas1ApplicationPlacementStatus = freshCase.cas1Application?.placementStatus
    cas2CourtBailApplicationId = freshCase.cas2CourtBailApplication?.id
    cas2PrisonBailApplicationId = freshCase.cas2PrisonBailApplication?.id
    cas2HdcApplicationId = freshCase.cas2HdcApplication?.id
    cas3ApplicationId = null
    cas3ApplicationApplicationStatus = null
    cas3ApplicationPlacementStatus = null
    // TODO there is a repo for dtr to get this data which we own - actually don't have this in here
    dtrStatus = null
    crsStatus = null
    nextAccommodationId = null
    currentAccommodationArrangementType = null
  }

  companion object {
    fun hydrate(
      id: UUID,
      crn: String,
      tier: TierScore?,
    ) = CaseAggregate(
      id = id,
      crn = crn,
      tier = tier,
    )

    fun createNew(
      id: UUID,
      crn: String,
    ) = CaseAggregate(
      id = id,
      crn = crn,
    )
  }

  data class CaseSnapshot(
    val id: UUID,
    val crn: String,
    val tier: TierScore?,
    val cas1ApplicationId: UUID?,
    val cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    val cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
    val cas2CourtBailApplicationId: UUID?,
    val cas2PrisonBailApplicationId: UUID?,
    val cas2HdcApplicationId: UUID?,
    val cas3ApplicationId: UUID?,
    val cas3ApplicationApplicationStatus: Cas3ApplicationStatus?,
    val cas3ApplicationPlacementStatus: Cas3PlacementStatus?,
    val dtrStatus: String?,
    val crsStatus: String?,
    val releaseDate: LocalDate?,
    val name: String?,
    val dateOfBirth: LocalDate?,
    val pncNumber: String?,
    val nomsNumber: String?,
    val sexCode: SexCode?,
    val nextAccommodationId: UUID?,
    val currentAccommodationArrangementType: AccommodationArrangementType?,
    val riskLevel: RiskLevel?,
    val teamDescription: String?,
    val teamCode: String?,
    val staffName: String?,
    val staffCode: String?,
  )

  fun snapshot() = CaseSnapshot(
    id,
    crn,
    tier,
    cas1ApplicationId,
    cas1ApplicationApplicationStatus,
    cas1ApplicationPlacementStatus,
    cas2CourtBailApplicationId,
    cas2PrisonBailApplicationId,
    cas2HdcApplicationId,
    cas3ApplicationId,
    cas3ApplicationApplicationStatus,
    cas3ApplicationPlacementStatus,
    dtrStatus,
    crsStatus,
    releaseDate,
    name,
    dateOfBirth,
    pncNumber,
    nomsNumber,
    sexCode,
    nextAccommodationId,
    currentAccommodationArrangementType,
    riskLevel,
    teamDescription,
    teamCode,
    staffName,
    staffCode,
  )

  fun toCprFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toFullName(name: Name) = listOfNotNull(
    name.forename,
    name.middleName,
    name.surname,
  ).joinToString(" ")

  fun toStaffFullName(name: StaffName) = listOfNotNull(
    name.forename,
    name.surname,
  ).joinToString(" ")
}
