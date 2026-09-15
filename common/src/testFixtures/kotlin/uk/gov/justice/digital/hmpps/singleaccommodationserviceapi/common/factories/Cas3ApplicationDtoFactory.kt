package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ExternalPreviousBookingCancellationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ExternalPreviousBookingDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3StaffDto
import java.time.LocalDate
import java.util.UUID

fun buildCas3ApplicationDto(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas3ApplicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
  assessmentStatus: Cas3AssessmentStatus? = null,
  bookingStatus: Cas3BookingStatus? = null,
  applicationSubmittedDate: LocalDate? = null,
  applicationSubmittedBy: Cas3StaffDto = buildCas3StaffDto(),
  applicationRejectedReason: String? = null,
  bookingProvisionalOfferSentDate: LocalDate? = null,
  previousBookings: List<Cas3ExternalPreviousBookingDto>? = null,
  premises: Cas3PremisesSummaryDto? = null,
  uiUrl: String = "https://cas3-ui/referrals/$id/full",
) = Cas3ApplicationDto(
  id = id,
  applicationStatus = applicationStatus,
  bookingStatus = bookingStatus,
  assessmentStatus = assessmentStatus,
  applicationSubmittedDate = applicationSubmittedDate,
  applicationSubmittedBy = applicationSubmittedBy,
  applicationRejectedReason = applicationRejectedReason,
  bookingProvisionalOfferSentDate = bookingProvisionalOfferSentDate,
  previousBookings = previousBookings,
  premises = premises,
  uiUrl = uiUrl,
)

fun buildCas3StaffDto(
  name: String = "Test Tester",
  username: String = "TestTester",
  staffCode: String = "Test1234",
) = Cas3StaffDto(
  name = name,
  username = username,
  staffCode = staffCode,
)

fun buildCas3ExternalPreviousBookingDto(
  bookingStatus: Cas3BookingStatus? = null,
  cancellation: Cas3ExternalPreviousBookingCancellationDto? = null,
) = Cas3ExternalPreviousBookingDto(
  bookingStatus = bookingStatus,
  cancellation = cancellation,
)

fun buildCas3ExternalPreviousBookingCancellationDto(
  cancellationDate: LocalDate? = null,
  cancellationReason: String? = null,
) = Cas3ExternalPreviousBookingCancellationDto(
  cancellationDate = cancellationDate,
  cancellationReason = cancellationReason,
)

fun buildCas3PremisesSummaryDto(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
  name: String? = "Test Premises",
) = Cas3PremisesSummaryDto(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
  name = name,
)
