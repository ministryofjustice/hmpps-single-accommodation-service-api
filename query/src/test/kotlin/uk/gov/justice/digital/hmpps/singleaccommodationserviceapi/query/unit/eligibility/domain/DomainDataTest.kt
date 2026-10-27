package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DomainDataTest {

  @Test
  fun `Create DomainData from external data`() {
    val crn = "ABC1234"

    val endDate = LocalDate.now().plusDays(1)

    val dutyToRefer = buildDutyToReferDto()

    val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices()
    val sexualOffenceRegistrations = buildSexualOffenceRegistrations(crn = crn)

    val currentAccommodation = buildAccommodationSummaryDto(
      endDate = endDate,
      type = buildAccommodationTypeDto(
        code = "A02",
      ),
    )

    val cas1Application = buildCas1Application(
      application = buildCas1ApplicationSummary(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
    )
    val cas2Application = buildCas2Application(
      submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = Cas2AssessmentStatus.PLACE_OFFERED),
    )
    val cas3Application = buildCas3Application(
      UUID.randomUUID(),
      applicationStatus = Cas3ApplicationStatus.SUBMITTED,
      submittedApplication = buildCas3SubmittedApplication(
        assessmentStatus = null,
        latestBooking = buildCas3LatestBooking(
          status = null,
        ),
      ),
    )
    val cpr = buildCorePersonRecord(
      dateOfBirth = LocalDate.parse("1990-01-01"),
      sex = buildSex(
        code = SexCode.M,
      ),
    )

    val tier = Tier(
      tierScore = "A1",
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )

    val currentAccommodationTypeEntity = buildAccommodationTypeEntity(
      code = "A02",
      isCas1 = true,
    )

    val nextAccommodations = listOf(
      buildAccommodationSummaryDto(
        status = buildAccommodationStatusDto(
          code = "PR1",
        ),
      ),
    )

    val expected = buildDomainData(
      crn = crn,
      tierScore = tier.tierScore,
      sex = cpr.sex?.code,
      dob = cpr.dateOfBirth,
      currentAccommodation = currentAccommodation,
      nextAccommodations = nextAccommodations,
      cas1Application = cas1Application,
      cas2Application = cas2Application,
      cas3Application = cas3Application,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
      currentAccommodationTypeEntity = currentAccommodationTypeEntity,
      sexualOffenceRegistrations = sexualOffenceRegistrations,
    )

    val result = DomainData(
      crn = crn,
      cpr = cpr,
      tier = tier,
      cas1Application = cas1Application,
      cas2Application = cas2Application,
      cas3Application = cas3Application,
      currentAccommodation = currentAccommodation,
      accommodationTypes = listOf(currentAccommodationTypeEntity),
      nextAccommodations = nextAccommodations,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
      sexualOffenceRegistrations = sexualOffenceRegistrations,
    )

    assertThat(result).isEqualTo(expected)
  }
}
