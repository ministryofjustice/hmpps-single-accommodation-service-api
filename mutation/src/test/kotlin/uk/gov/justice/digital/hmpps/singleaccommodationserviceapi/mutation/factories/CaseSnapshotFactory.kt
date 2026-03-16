package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildCaseSnapshot(
  crn: String = "X12345",
  tier: TierScore? = TierScore.A1,
  staffCode: String? = "1L123",
  staffName: String? = "Test Staff",
  teamCode: String? = "1L123",
  teamDescription: String? = "Test Description",
  riskLevel: RiskLevel? = RiskLevel.HIGH,
  currentAccommodationArrangementType: AccommodationArrangementType? = null,
  nextAccommodationId: UUID? = null,
  sexCode: SexCode? = SexCode.M,
  nomsNumber: String? = null,
  pncNumber: String? = null,
  dateOfBirth: LocalDate? = LocalDate.now().minusYears(25),
  name: String? = "Test Name",
  releaseDate: LocalDate? = LocalDate.now().plusDays(25),
  crsStatus: String? = null,
  dtrStatus: String? = null,
  cas3ApplicationPlacementStatus: Cas3PlacementStatus? = null,
  cas3ApplicationApplicationStatus: Cas3ApplicationStatus? = null,
  cas3ApplicationId: UUID? = null,
  cas2HdcApplicationId: UUID? = null,
  cas2PrisonBailApplicationId: UUID? = null,
  cas2CourtBailApplicationId: UUID? = null,
  cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
  cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  cas1ApplicationId: UUID? = null,
) = CaseSnapshot(
  id = UUID.randomUUID(),
  crn = crn,
  tier = tier,
  cas1ApplicationId = cas1ApplicationId,
  cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
  cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
  cas2CourtBailApplicationId = cas2CourtBailApplicationId,
  cas2PrisonBailApplicationId = cas2PrisonBailApplicationId,
  cas2HdcApplicationId = cas2HdcApplicationId,
  cas3ApplicationId = cas3ApplicationId,
  cas3ApplicationApplicationStatus = cas3ApplicationApplicationStatus,
  cas3ApplicationPlacementStatus = cas3ApplicationPlacementStatus,
  dtrStatus = dtrStatus,
  crsStatus = crsStatus,
  releaseDate = releaseDate,
  name = name,
  dateOfBirth = dateOfBirth,
  pncNumber = pncNumber,
  nomsNumber = nomsNumber,
  sexCode = sexCode,
  nextAccommodationId = nextAccommodationId,
  currentAccommodationArrangementType = currentAccommodationArrangementType,
  riskLevel = riskLevel,
  teamDescription = teamDescription,
  teamCode = teamCode,
  staffName = staffName,
  staffCode = staffCode,
)
