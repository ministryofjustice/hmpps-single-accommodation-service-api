package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas1Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.STARTED,
  placementStatus: Cas1PlacementStatus? = null,
  requestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  premises: Cas1PremisesSummary? = null,
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
) = Cas1Application(
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

fun buildCas1PremisesSummary(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
) = Cas1PremisesSummary(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
)

fun buildCas3Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas3ApplicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
  assessmentStatus: Cas3AssessmentStatus? = null,
  bookingStatus: Cas3BookingStatus? = null,
  premises: Cas3PremisesSummary? = null,
  uiUrl: String = "https://cas3-ui/referrals/$id/full",
) = Cas3Application(
  id = id,
  applicationStatus = applicationStatus,
  bookingStatus = bookingStatus,
  assessmentStatus = assessmentStatus,
  premises = premises,
  uiUrl = uiUrl,
)

fun buildCas3PremisesSummary(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
  name: String? = "Test Premises",
) = Cas3PremisesSummary(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
  name = name,
)
