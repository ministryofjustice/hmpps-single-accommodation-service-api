package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalPremisesV2Dto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementWithdrawal
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ExternalPreviousBookingCancellation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PreviousBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3SubmittedApplication
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas2Application(
  submittedApplication: Cas2SubmittedApplicationSummary? = null,
  id: UUID = UUID.randomUUID(),
  uiUrl: String = "https://cas2-ui/applications/$id",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdBy: Cas2Staff = buildCas2Staff(),
  cohort: String? = null,
) = Cas2Application(
  uiUrl = uiUrl,
  id = id,
  submittedApplication = submittedApplication,
  createdAt = createdAt,
  createdBy = createdBy,
  cohort = cohort,
)

fun buildCas2SubmittedApplicationSummary(
  latestAssessmentStatus: Cas2AssessmentStatus? = null,
  submittedAt: OffsetDateTime = OffsetDateTime.now(),
  offerDeclinedReason: String? = null,
  cancelledReason: String? = null,
  markedArrivedDateTime: Instant? = null,
) = Cas2SubmittedApplicationSummary(
  submittedAt = submittedAt,
  latestAssessmentStatus = latestAssessmentStatus,
  offerDeclinedReason = offerDeclinedReason,
  cancelledReason = cancelledReason,
  markedAsArrivedDateTime = markedArrivedDateTime,
)

fun buildCas2Staff(
  name: String = "Test Tester",
  username: String = "testTester@gov.uk",
  deliusStaffCode: String? = "ABCD123",
  nomisStaffId: Long? = null,
  userType: String = "NOMIS",
) = Cas2Staff(
  name = name,
  username = username,
  deliusStaffCode = deliusStaffCode,
  nomisStaffId = nomisStaffId,
  userType = userType,
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

fun buildCas1AssessmentSummary(
  rejectionRationale: String? = null,
  decision: String? = null,
) = Cas1AssessmentSummary(
  decision = decision,
  rejectionRationale = rejectionRationale,
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
  submittedApplication: Cas3SubmittedApplication? = when (applicationStatus) {
    Cas3ApplicationStatus.IN_PROGRESS -> null
    Cas3ApplicationStatus.SUBMITTED -> buildCas3SubmittedApplication()
    Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> buildCas3SubmittedApplication()
    Cas3ApplicationStatus.REJECTED -> buildCas3SubmittedApplication()
  },
  uiUrl: String = "https://cas3-ui/referrals/$id/full",
) = Cas3Application(
  id = id,
  applicationStatus = applicationStatus,
  submittedApplication = submittedApplication,
  uiUrl = uiUrl,
)

fun buildCas3SubmittedApplication(
  submittedDate: LocalDate = LocalDate.now(),
  submittedBy: Cas3Staff = buildCas3Staff(),
  assessmentStatus: Cas3AssessmentStatus? = null,
  assessmentRejectionReason: String? = null,
  latestBooking: Cas3LatestBooking? = null,
  previousBookings: List<Cas3PreviousBooking>? = emptyList(),
) = Cas3SubmittedApplication(
  submittedDate,
  submittedBy,
  assessmentStatus,
  assessmentRejectionReason,
  latestBooking,
  previousBookings,
)

fun buildCas3LatestBooking(
  status: Cas3BookingStatus? = null,
  provisionalOfferSentDate: LocalDate? = null,
  premises: Cas3BookingPremises = buildCas3LatestBookingPremisesDto(),
  cancellation: Cas3ExternalPreviousBookingCancellation? = null,
) = Cas3LatestBooking(
  status = status,
  provisionalOfferSentDate = provisionalOfferSentDate,
  premises = premises,
  cancellation = cancellation,
)

fun buildCas3LatestBookingPremisesDto() = Cas3BookingPremises(
  startDate = LocalDate.now().plusDays(1),
  endDate = LocalDate.now().plusDays(10),
  addressLine1 = "123 Test Street",
  addressLine2 = "Test Village",
  town = "Test Town",
  postcode = "AB1 2CD",
  name = "Test Premises",
)

fun buildCas3Staff(
  name: String = "Test Tester",
  username: String = "TestTester",
  staffCode: String = "Test1234",
) = Cas3Staff(
  name = name,
  username = username,
  staffCode = staffCode,
)

fun buildCas3ExternalPreviousBooking(
  bookingStatus: Cas3BookingStatus? = null,
  cancellation: Cas3ExternalPreviousBookingCancellation? = null,
) = Cas3PreviousBooking(
  bookingStatus = bookingStatus,
  cancellation = cancellation,
)

fun buildCas3ExternalPreviousBookingCancellation(
  cancellationDate: LocalDate? = null,
  cancellationReason: String? = null,
) = Cas3ExternalPreviousBookingCancellation(
  cancellationDate = cancellationDate,
  cancellationReason = cancellationReason,
)

fun buildCas3PremisesSummary(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
  name: String? = "Test Premises",
) = Cas3BookingPremises(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
  name = name,
)

fun buildCas1ApplicationV2(
  id: UUID = UUID.randomUUID(),
  status: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdBy: Cas1Staff = buildCas1Staff(),
  expiresAt: LocalDate? = null,
  submittedApplication: Cas1SubmittedApplication? = null,
  uiUrl: String = "https://cas1-ui/applications/$id",
) = Cas1ApplicationV2(
  uiUrl = uiUrl,
  id = id,
  status = status,
  createdAt = createdAt,
  createdBy = createdBy,
  expiresAt = expiresAt,
  submittedApplication = submittedApplication,
)

fun buildCas1SubmittedApplication(
  submittedAt: OffsetDateTime = OffsetDateTime.now(),
  assessment: Cas1AssessmentSummary? = null,
  upcomingRequestForPlacement: Cas1ExternalRequestForPlacementOutcome? = null,
  requestForPlacementHistory: List<Cas1ExternalRequestForPlacementOutcome> = emptyList(),
) = Cas1SubmittedApplication(
  submittedAt = submittedAt,
  assessment = assessment,
  upcomingRequestForPlacement = upcomingRequestForPlacement,
  requestForPlacementHistory = requestForPlacementHistory,
)

fun buildCas1ExternalRequestForPlacementOutcomeDto(
  requestForPlacement: Cas1RequestForPlacementSummaryV2 = buildCas1RequestForPlacementSummaryV2(),
  placement: Cas1PlacementV2? = null,
) = Cas1ExternalRequestForPlacementOutcome(
  requestForPlacement = requestForPlacement,
  placement = placement,
)

fun buildCas1RequestForPlacementSummaryV2(
  status: Cas1RequestForPlacementStatus = Cas1RequestForPlacementStatus.REQUEST_SUBMITTED,
  statusLastUpdated: LocalDateTime = LocalDateTime.now(),
  submittedBy: Cas1Staff = buildCas1Staff(),
  submittedAt: LocalDate = LocalDate.now(),
  expectedArrivalDate: LocalDate = LocalDate.now(),
  durationDays: Int? = null,
  decision: String? = null,
  rejectionReason: String? = null,
  withdrawal: Cas1RequestForPlacementWithdrawal? = null,
) = Cas1RequestForPlacementSummaryV2(
  status = status,
  statusLastUpdated = statusLastUpdated,
  submittedBy = submittedBy,
  submittedAt = submittedAt,
  expectedArrivalDate = expectedArrivalDate,
  durationDays = durationDays,
  decision = decision,
  rejectionReason = rejectionReason,
  withdrawal = withdrawal,
)

fun buildCas1PlacementV2(
  status: Cas1PlacementStatus = Cas1PlacementStatus.ARRIVED,
  statusLastUpdated: LocalDateTime = LocalDateTime.now(),
  canonicalArrivalDate: LocalDate = LocalDate.now(),
  canonicalDepartureDate: LocalDate = LocalDate.now().plusMonths(6),
  actualArrivalDate: LocalDate? = null,
  actualDepartureDate: LocalDate? = null,
  expectedArrivalDate: LocalDate = LocalDate.now(),
  expectedDepartureDate: LocalDate = LocalDate.now().plusMonths(6),
  cancellationReason: String? = null,
  premises: Cas1ExternalPremisesV2Dto = buildCas1ExternalPremisesV2Dto(),
) = Cas1PlacementV2(
  status = status,
  statusLastUpdated = statusLastUpdated,
  canonicalArrivalDate = canonicalArrivalDate,
  canonicalDepartureDate = canonicalDepartureDate,
  actualArrivalDate = actualArrivalDate,
  actualDepartureDate = actualDepartureDate,
  expectedArrivalDate = expectedArrivalDate,
  expectedDepartureDate = expectedDepartureDate,
  cancellationReason = cancellationReason,
  premises = premises,
)

fun buildCas1ExternalPremisesV2Dto(
  addressLine1: String = "TEST HOUSE",
  addressLine2: String? = null,
  town: String? = null,
  postcode: String = "TEST POSTCODE",
) = Cas1ExternalPremisesV2Dto(
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
)
