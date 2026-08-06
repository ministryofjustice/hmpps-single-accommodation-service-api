package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas1ApplicationDto(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  placementStatus: Cas1PlacementStatus? = null,
  requestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  premises: Cas1PremisesSummaryDto? = null,
  uiUrl: String = "https://cas1-ui/applications/$id",
  applicationStartedAt: OffsetDateTime = OffsetDateTime.now(),
  applicationStartedBy: String = "A User",
  applicationSubmittedAt: OffsetDateTime? = null,
  applicationSubmittedBy: String? = null,
  applicationExpiresAt: LocalDate? = null,
  assessmentDecision: String? = null,
  assessmentRejectionRationale: String? = null,
  requestForPlacementDecision: String? = null,
  requestForPlacementRejectionReason: String? = null,
  requestSubmittedBy: String? = null,
  requestSubmittedAt: LocalDate? = null,
  withdrawalReason: String? = null,
  withdrawalDate: LocalDate? = null,
  actualArrivalDate: LocalDate? = null,
  actualDepartureDate: LocalDate? = null,
  cancellationReason: String? = null,
  expectedArrivalDate: LocalDate? = null,
  duration: Int? = null,
) = Cas1ApplicationDto(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
  requestForPlacementStatus = requestForPlacementStatus,
  premises = premises,
  uiUrl = uiUrl,
  applicationStartedAt = applicationStartedAt,
  applicationStartedBy = applicationStartedBy,
  applicationSubmittedAt = applicationSubmittedAt,
  applicationSubmittedBy = applicationSubmittedBy,
  applicationExpiresAt = applicationExpiresAt,
  assessmentDecision = assessmentDecision,
  assessmentRejectionRationale = assessmentRejectionRationale,
  requestForPlacementDecision = requestForPlacementDecision,
  requestForPlacementRejectionReason = requestForPlacementRejectionReason,
  requestSubmittedBy = requestSubmittedBy,
  requestSubmittedAt = requestSubmittedAt,
  withdrawalReason = withdrawalReason,
  withdrawalDate = withdrawalDate,
  actualArrivalDate = actualArrivalDate,
  actualDepartureDate = actualDepartureDate,
  cancellationReason = cancellationReason,
  expectedArrivalDate = expectedArrivalDate,
  duration = duration,
)
