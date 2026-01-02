package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.Rosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.determineOverallRiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.toRiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra

class RiskLevelTransformerTest {

  @ParameterizedTest(name = "determineOverallRiskLevel() should return {0}")
  @EnumSource(RiskLevelInfra::class)
  fun `overall risk level test`(expectedOverallRiskLevel: RiskLevelInfra) {
    val rosh = Rosh(
      riskChildrenCommunity = expectedOverallRiskLevel,
      riskPrisonersCustody = RiskLevelInfra.LOW,
      riskStaffCommunity = RiskLevelInfra.LOW,
      riskStaffCustody = RiskLevelInfra.LOW,
      riskKnownAdultCommunity = RiskLevelInfra.LOW,
      riskKnownAdultCustody = RiskLevelInfra.LOW,
      riskPublicCommunity = RiskLevelInfra.LOW,
      riskPublicCustody = RiskLevelInfra.LOW,
      riskChildrenCustody = RiskLevelInfra.LOW,
    )

    val overallRiskLevel = determineOverallRiskLevel(rosh)
    assertThat(overallRiskLevel).isEqualTo(toRiskLevel(expectedOverallRiskLevel))
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
    assertThat(determineOverallRiskLevel(rosh)).isNull()
  }
}
