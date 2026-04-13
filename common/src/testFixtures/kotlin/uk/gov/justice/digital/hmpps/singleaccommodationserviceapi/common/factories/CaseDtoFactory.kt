package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import java.time.LocalDate

fun buildCaseDto(
  crn: String,
  name: String = "First Middle Last",
  dateOfBirth: LocalDate? = LocalDate.of(2000, 12, 3),
  prisonNumber: String? = "PRI1",
  tierScore: TierScore? = TierScore.A1,
  tier: TierScore? = TierScore.A1,
  riskLevel: RiskLevel? = RiskLevel.VERY_HIGH,
  pncReference: String? = "Some PNC Reference",
  assignedTo: AssignedToDto? = buildAssignedToDto(),
  currentAccommodation: AccommodationDetail? = null,
  nextAccommodation: AccommodationDetail? = null,
  status: Status? = null,
  actions: List<String> = emptyList(),
) = CaseDto(
  name,
  dateOfBirth,
  crn,
  prisonNumber,
  photoUrl = null,
  tierScore,
  tier,
  riskLevel,
  pncReference,
  assignedTo,
  currentAccommodation,
  nextAccommodation,
  status,
  actions,
)

fun buildAssignedToDto(
  id: Long? = 1L,
  name: String = "First Middle Last",
  username: String? = "user1",
  staffCode: String? = "ABCD1234",
) = AssignedToDto(
  id,
  name,
  username,
  staffCode,
)
