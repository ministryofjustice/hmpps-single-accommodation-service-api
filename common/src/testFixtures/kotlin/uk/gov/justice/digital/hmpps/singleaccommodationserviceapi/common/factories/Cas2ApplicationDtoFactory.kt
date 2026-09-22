package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2AssessmentStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2CohortDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2SubmittedApplicationSummaryDto
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas2ApplicationDto(
  submittedApplication: Cas2SubmittedApplicationSummaryDto? = null,
  id: UUID = UUID.randomUUID(),
  uiUrl: String = "https://cas2-ui/applications/$id",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdBy: Cas2StaffDto = buildCas2StaffDto(),
  cohort: Cas2CohortDto? = null,
) = Cas2ApplicationDto(
  uiUrl = uiUrl,
  id = id,
  submittedApplication = submittedApplication,
  createdAt = createdAt,
  createdBy = createdBy,
  cohort = cohort,
)

fun buildCas2SubmittedApplicationSummaryDto(
  latestAssessmentStatus: Cas2AssessmentStatusDto? = null,
  submittedAt: OffsetDateTime = OffsetDateTime.now(),
  offerDeclinedReason: String? = null,
  cancelledReason: String? = null,
) = Cas2SubmittedApplicationSummaryDto(
  submittedAt = submittedAt,
  latestAssessmentStatus = latestAssessmentStatus,
  offerDeclinedReason = offerDeclinedReason,
  cancelledReason = cancelledReason,
)

fun buildCas2StaffDto(
  name: String = "Test Tester",
  username: String = "testTester@gov.uk",
  deliusStaffCode: String? = "ABCD123",
  nomisStaffId: Long? = null,
  userType: String = "NOMIS",
) = Cas2StaffDto(
  name = name,
  username = username,
  deliusStaffCode = deliusStaffCode,
  nomisStaffId = nomisStaffId,
  userType = userType,
)
