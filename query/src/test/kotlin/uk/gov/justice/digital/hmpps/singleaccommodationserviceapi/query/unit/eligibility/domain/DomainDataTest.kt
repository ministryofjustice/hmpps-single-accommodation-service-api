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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
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
    )

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `Create DomainData from internal data`() {
    val dutyToRefer = buildDutyToReferDto()
    val crn = "ABC1234"
    val tierScore = "A1"
    val personDto = buildFullPersonDto(
      crn = crn,
      gender = "Male",
    )
    val caseEntity = buildCaseEntity(
      tierScore = tierScore,
      dateOfBirth = LocalDate.parse("1990-01-01"),
    ) { withCrn(crn) }
    val expected = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      dob = caseEntity.dateOfBirth,
      sex = SexCode.M,
      cas1Application = null,
      cas3Application = null,
      currentAccommodation = null,
      currentAccommodationTypeEntity = null,
      nextAccommodations = emptyList(),
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = null,
    )
    val result = DomainData(
      crn = crn,
      sexCode = SexCode.findByGender(personDto.gender),
      caseEntity = caseEntity,
      dutyToRefer = dutyToRefer,
    )
    assertThat(result).isEqualTo(expected)
  }
}
