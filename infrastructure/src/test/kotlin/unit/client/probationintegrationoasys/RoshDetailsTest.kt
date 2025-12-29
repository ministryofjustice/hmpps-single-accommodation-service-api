package unit.client.probationintegrationoasys

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.Rosh

class RoshDetailsTest {
  @ParameterizedTest(name = "determineOverallRiskLevel() should return {0}")
  @EnumSource(RiskLevel::class)
  fun `overall risk level test`(expectedOverallRiskLevel: RiskLevel) {
    val rosh = Rosh(
      riskChildrenCommunity = expectedOverallRiskLevel,
      riskPrisonersCustody = RiskLevel.LOW,
      riskStaffCommunity = RiskLevel.LOW,
      riskStaffCustody = RiskLevel.LOW,
      riskKnownAdultCommunity = RiskLevel.LOW,
      riskKnownAdultCustody = RiskLevel.LOW,
      riskPublicCommunity = RiskLevel.LOW,
      riskPublicCustody = RiskLevel.LOW,
      riskChildrenCustody = RiskLevel.LOW,
    )

    val overallRiskLevel = rosh.determineOverallRiskLevel()
    assertThat(overallRiskLevel).isEqualTo(expectedOverallRiskLevel)
  }

  @Test
  fun `overall risk level test when null`() {
    val rosh = Rosh(
      riskChildrenCommunity = null,
      riskPrisonersCustody = null,
      riskStaffCommunity = null,
      riskStaffCustody = null,
      riskKnownAdultCommunity = null,
      riskKnownAdultCustody = null,
      riskPublicCommunity = null,
      riskPublicCustody = null,
      riskChildrenCustody = null,
    )
    assertThat(rosh.determineOverallRiskLevel()).isNull()
  }
}
