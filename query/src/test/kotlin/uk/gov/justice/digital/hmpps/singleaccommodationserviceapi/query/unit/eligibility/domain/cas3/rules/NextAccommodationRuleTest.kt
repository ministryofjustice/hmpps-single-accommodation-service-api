package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NextAccommodationRule
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class NextAccommodationRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @Test
  fun `candidate passes when next accommodation is null`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      nextAccommodation = null,
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AccommodationArrangementType::class)
  fun `candidate fails when next accommodation exists`(accommodationType: AccommodationArrangementType) {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      nextAccommodation = buildAccommodationDetail(accommodationType),
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }

  private fun buildAccommodationDetail(type: AccommodationArrangementType) = AccommodationDetail(
    id = UUID.randomUUID(),
    name = null,
    arrangementType = type,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    verificationStatus = null,
    nextAccommodationStatus = null,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null
    ),
    startDate = null,
    endDate = null,
    createdAt = Instant.now(),
  )
}
