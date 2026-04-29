package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPersonDto
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

    val currentAccommodation = buildCurrentAccommodation(
      endDate,
      isPrisonCas1Cas2OrCas2v2 = true,
      isPrivate = false,
    )

    val cas1Application = buildCas1Application(
      UUID.randomUUID(),
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      null,
      null,
    )
    val cas3Application = buildCas3Application(
      UUID.randomUUID(),
      applicationStatus = Cas3ApplicationStatus.SUBMITTED,
      bookingStatus = null,
      assessmentStatus = null,
    )
    val cpr = buildCorePersonRecord()

    val currentAccommodationSummary = buildAccommodationSummaryDto(
      endDate = endDate,
    )

    val tier = Tier(
      tierScore = TierScore.A1,
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )

    val expected = buildDomainData(
      crn = crn,
      tierScore = tier.tierScore,
      sex = cpr.sex?.code,
      currentAccommodation = currentAccommodation,
      hasNextAccommodation = false,
      cas1Application = cas1Application,
      cas3Application = cas3Application,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
    )

    val result = DomainData(
      crn = crn,
      cpr = cpr,
      tier = tier,
      cas1Application = cas1Application,
      cas3Application = cas3Application,
      currentAccommodationSummary = currentAccommodationSummary,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = commissionedRehabilitativeServices,
    )

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `Create DomainData from internal data`() {
    val dutyToRefer = buildDutyToReferDto()
    val crn = "ABC1234"
    val tierScore = TierScore.A1
    val personDto = buildPersonDto(
      crn = crn,
      gender = "Male",
    )
    val caseEntity = buildCaseEntity(
      tierScore = tierScore,
    ) { withCrn(crn) }
    val expected = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      cas1Application = null,
      cas3Application = null,
      currentAccommodation = null,
      hasNextAccommodation = false,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = null,
    )
    val result = DomainData(
      personDto = personDto,
      caseEntity = caseEntity,
      dutyToRefer = dutyToRefer,
    )
    assertThat(result).isEqualTo(expected)
  }
}
