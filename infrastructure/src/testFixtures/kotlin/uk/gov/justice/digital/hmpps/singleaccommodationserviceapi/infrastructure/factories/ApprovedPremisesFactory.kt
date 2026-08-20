package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementPair
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas1Application(
  application: Cas1ApplicationSummary = buildCas1ApplicationSummary(),
  assessment: Cas1AssessmentSummary? = null,
  requestForPlacement: Cas1RequestForPlacementSummary? = null,
  placement: Cas1PlacementSummary? = null,
  placementHistory: List<Cas1PlacementPair> = emptyList(),
  uiUrl: String = "https://cas1-ui/applications/${application.id}",
) = Cas1Application(
  uiUrl = uiUrl,
  application = application,
  assessment = assessment,
  requestForPlacement = requestForPlacement,
  placement = placement,
  placementHistory = placementHistory,
)

fun buildCas1PlacementPair(
  requestForPlacement: Cas1RequestForPlacementSummary = buildCas1RequestForPlacementSummary(),
  placement: Cas1PlacementSummary? = null,
  dateApplied: LocalDate = LocalDate.now(),
) = Cas1PlacementPair(
  requestForPlacement = requestForPlacement,
  placement = placement,
  dateApplied = dateApplied,
)

fun buildCas1PremisesSummary(
  addressLine1: String = "TEST HOUSE",
  addressLine2: String? = null,
  town: String? = null,
  postcode: String = "TEST POSTCODE",
  startDate: LocalDate = LocalDate.now(),
  endDate: LocalDate? = null,
) = Cas1PremisesSummary(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
)

fun buildCas1PlacementSummary(
  status: Cas1PlacementStatus? = null,
  cancellationReason: String? = null,
  actualDepartureDate: LocalDate? = null,
  actualArrivalDate: LocalDate? = null,
  premises: Cas1PremisesSummary? = null,
) = Cas1PlacementSummary(
  status = status,
  actualArrivalDate = actualArrivalDate,
  actualDepartureDate = actualDepartureDate,
  cancellationReason = cancellationReason,
  premises = premises,
)

fun buildCas1RequestForPlacementSummary(
  status: Cas1RequestForPlacementStatus? = null,
  rejectionReason: String? = null,
  submittedBy: Cas1Staff? = null,
  submittedAt: LocalDate? = null,
  withdrawalReason: String? = null,
  withdrawalDate: LocalDate? = null,
  expectedArrivalDate: LocalDate? = null,
  durationDays: Int? = null,
  decision: String? = null,
) = Cas1RequestForPlacementSummary(
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

fun buildCas1AssessmentSummary(
  rejectionRationale: String? = null,
  decision: AssessmentDecision? = null,
) = Cas1AssessmentSummary(
  decision = decision,
  rejectionRationale = rejectionRationale,
)

fun buildCas1ApplicationSummary(
  id: UUID = UUID.randomUUID(),
  status: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdBy: Cas1Staff = buildCas1Staff(),
  submittedAt: OffsetDateTime? = null,
  expiresAt: LocalDate? = null,
) = Cas1ApplicationSummary(
  id = id,
  status = status,
  createdAt = createdAt,
  createdBy = createdBy,
  submittedAt = submittedAt,
  expiresAt = expiresAt,
)

fun buildCas1Staff(
  name: String = "Test Tester",
  username: String = "testTester",
  staffCode: String = "1234",
) = Cas1Staff(
  name = name,
  username = username,
  staffCode = staffCode,
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
