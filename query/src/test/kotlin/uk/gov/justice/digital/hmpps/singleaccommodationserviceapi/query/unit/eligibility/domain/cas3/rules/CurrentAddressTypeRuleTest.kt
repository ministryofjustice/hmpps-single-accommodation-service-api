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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CurrentAddressTypeRule
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class CurrentAddressTypeRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, names = ["PRISON", "CAS1", "CAS2", "CAS2V2"])
  fun `candidate passes when current accommodation is eligible type`(accommodationType: AccommodationArrangementType) {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = buildAccommodationDetail(accommodationType),
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, names = ["CAS3", "PRIVATE", "NO_FIXED_ABODE"])
  fun `candidate fails when current accommodation is ineligible type`(accommodationType: AccommodationArrangementType) {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = buildAccommodationDetail(accommodationType),
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate fails when current accommodation is null`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = null,
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAddressTypeRule().description)
      .isEqualTo("FAIL if current address is not Approved Premise (CAS1), CAS2, or Prison")
  }

  private fun buildAccommodationDetail(type: AccommodationArrangementType) = AccommodationDetail(
    id = UUID.randomUUID(),
    crn = crn,
    arrangementType = type,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    name = null,
    settledType = AccommodationSettledType.TRANSIENT,
    offenderReleaseType = null,
    verificationStatus = null,
    nextAccommodationStatus = null,
    startDate = null,
    endDate = null,
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
      uprn = null,
    ),
    createdBy = "Joe Bloggs",
    createdAt = Instant.now(),
  )
}
