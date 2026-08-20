package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1AssessmentSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementPairDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PlacementApplicationDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawPlacementRequestReason
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas1ApplicationDto(
  application: Cas1ApplicationSummaryDto = buildCas1ApplicationSummaryDto(),
  assessment: Cas1AssessmentSummaryDto? = null,
  requestForPlacement: Cas1RequestForPlacementSummaryDto? = null,
  placement: Cas1PlacementSummaryDto? = null,
  placementHistory: List<Cas1PlacementPairDto> = emptyList(),
  uiUrl: String = "https://cas1-ui/applications/${application.id}",
) = Cas1ApplicationDto(
  id = application.id,
  applicationStatus = application.status,
  placementStatus = placement?.status,
  requestForPlacementStatus = requestForPlacement?.status,
  uiUrl = uiUrl,
  application = application,
  assessment = assessment,
  requestForPlacement = requestForPlacement,
  placement = placement,
  placementHistory = placementHistory,
)

fun buildCas1PlacementPairDto(
  requestForPlacement: Cas1RequestForPlacementSummaryDto = buildCas1RequestForPlacementSummaryDto(),
  placement: Cas1PlacementSummaryDto? = null,
  dateApplied: LocalDate = LocalDate.now(),
) = Cas1PlacementPairDto(
  requestForPlacement = requestForPlacement,
  placement = placement,
  dateApplied = dateApplied,
)

fun buildCas1PremisesSummaryDto(
  addressLine1: String = "TEST HOUSE",
  addressLine2: String? = null,
  town: String? = null,
  postcode: String = "TEST POSTCODE",
  startDate: LocalDate = LocalDate.now(),
  endDate: LocalDate? = null,
) = Cas1PremisesSummaryDto(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
)

fun buildCas1PlacementSummaryDto(
  status: Cas1PlacementStatus? = null,
  cancellationReason: String? = null,
  actualDepartureDate: LocalDate? = null,
  actualArrivalDate: LocalDate? = null,
  premises: Cas1PremisesSummaryDto? = null,
) = Cas1PlacementSummaryDto(
  status = status,
  actualArrivalDate = actualArrivalDate,
  actualDepartureDate = actualDepartureDate,
  cancellationReason = cancellationReason,
  premises = premises,
)

fun buildCas1RequestForPlacementSummaryDto(
  status: Cas1RequestForPlacementStatus? = null,
  rejectionReason: String? = null,
  submittedBy: Cas1StaffDto? = null,
  submittedAt: LocalDate? = null,
  withdrawalReason: WithdrawPlacementRequestReason? = null,
  withdrawalDate: LocalDate? = null,
  expectedArrivalDate: LocalDate? = null,
  durationDays: Int? = null,
  decision: PlacementApplicationDecision? = null,
) = Cas1RequestForPlacementSummaryDto(
  status = status,
  decision = decision,
  rejectionReason = rejectionReason,
  submittedBy = submittedBy,
  submittedAt = submittedAt,
  withdrawalReason = withdrawalReason,
  withdrawalDate = withdrawalDate,
  expectedArrivalDate = expectedArrivalDate,
  durationDays = durationDays,
)

fun buildCas1AssessmentSummaryDto(
  rejectionRationale: String? = null,
  decision: AssessmentDecision? = null,
) = Cas1AssessmentSummaryDto(
  decision = decision,
  rejectionRationale = rejectionRationale,
)

fun buildCas1ApplicationSummaryDto(
  id: UUID = UUID.randomUUID(),
  status: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdBy: Cas1StaffDto = buildCas1StaffDto(),
  submittedAt: OffsetDateTime? = null,
  expiresAt: LocalDate? = null,
) = Cas1ApplicationSummaryDto(
  id = id,
  status = status,
  createdAt = createdAt,
  createdBy = createdBy,
  submittedAt = submittedAt,
  expiresAt = expiresAt,
)

fun buildCas1StaffDto(
  name: String = "Test Tester",
  username: String = "testTester",
  staffCode: String = "1234",
) = Cas1StaffDto(
  name = name,
  username = username,
  staffCode = staffCode,
)
