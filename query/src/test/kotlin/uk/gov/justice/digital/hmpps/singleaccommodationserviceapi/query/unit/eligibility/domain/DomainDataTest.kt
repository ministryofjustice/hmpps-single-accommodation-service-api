package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
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

    val currenAccommodation = buildCurrentAccommodation(
      endDate,
      true,
    )

    val expected = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      cas1Application = Cas1Application(
        UUID.randomUUID(),
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        null,
        null,
      ),
      cas3Application = Cas3Application(
        UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        bookingStatus = null,
        assessmentStatus = null,
      ),
      currentAccommodation = currenAccommodation,
      hasNextAccommodation = false,
    )
    val cpr = buildCorePersonRecord(
      sex = buildSex(SexCode.M),
      addresses = listOf(
        buildAddress(
          addressStatus = AddressStatus.M,
          endDate = LocalDate.now().plusDays(1),
        ),
      ),
    )
    val currentAccommodationSummary = buildAccommodationSummaryDto(
      endDate = endDate,
    )

    val tier = Tier(
      tierScore = expected.tierScore!!,
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )

    val result = DomainData(
      crn = expected.crn,
      cpr = cpr,
      tier = tier,
      cas1Application = expected.cas1Application,
      cas3Application = expected.cas3Application,
      currentAccommodationSummary = currentAccommodationSummary,
    )

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `Create DomainData from internal data`() {
    val crn = "ABC1234"
    val tierScore = TierScore.A1
    val cas1Application = Cas1Application(
      UUID.randomUUID(),
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      null,
      null,
    )
    val personDto = buildPersonDto(
      crn = crn,
      gender = "Male",
    )
    val caseEntity = buildCaseEntity(
      tierScore = tierScore,
      cas1ApplicationId = cas1Application.id,
      cas1ApplicationApplicationStatus = cas1Application.applicationStatus,
      cas1ApplicationPlacementStatus = cas1Application.placementStatus,
      cas1ApplicationRequestForPlacementStatus = cas1Application.requestForPlacementStatus,
    ) { withCrn(crn) }
    val expected = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      cas1Application = cas1Application,
      cas3Application = null,
      currentAccommodation = null,
      hasNextAccommodation = false,
    )
    val result = DomainData(
      personDto = personDto,
      caseEntity = caseEntity,
    )
    assertThat(result).isEqualTo(expected)
  }
}
