package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.util.UUID

class DeeplinkResolverTest {
  private val cas1UiUrl = "CAS1_UI_URL"
  private val cas3UiUrl = "CAS3_UI_URL"
  private val resolver = DeeplinkResolver(cas1UiUrl, cas3UiUrl)

  @Nested
  inner class Cas1 {
    @Test
    fun `CAS1_START_APPLICATION resolves to applications start path`() {
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS1_START_APPLICATION),
        buildDomainData(),
      )

      assertThat(result.url).isEqualTo("$cas1UiUrl/applications/start")
    }

    @Test
    fun `CAS1_VIEW_APPLICATION resolves to applications id path`() {
      val appId = UUID.randomUUID()
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS1_VIEW_APPLICATION),
        buildDomainData(cas1Application = buildCas1Application(id = appId)),
      )

      assertThat(result.url).isEqualTo("$cas1UiUrl/applications/$appId")
    }

    @Test
    fun `CAS1_VIEW_APPLICATION with no application resolves to null url`() {
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS1_VIEW_APPLICATION),
        buildDomainData(cas1Application = null),
      )

      assertThat(result.url).isNull()
    }
  }

  @Nested
  inner class Cas3 {
    @Test
    fun `CAS3_START_REFERRAL resolves to referrals start path`() {
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS3_START_REFERRAL),
        buildDomainData(),
      )

      assertThat(result.url).isEqualTo("$cas3UiUrl/referrals/start")
    }

    @Test
    fun `CAS3_VIEW_REFERRAL resolves to referrals id path`() {
      val referralId = UUID.randomUUID()
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS3_VIEW_REFERRAL),
        buildDomainData(cas3Application = buildCas3Application(id = referralId)),
      )

      assertThat(result.url).isEqualTo("$cas3UiUrl/referrals/$referralId/full")
    }

    @Test
    fun `CAS3_VIEW_REFERRAL with no application resolves to null url`() {
      val result = resolver.resolve(
        buildServiceResult(linkType = LinkType.CAS3_VIEW_REFERRAL),
        buildDomainData(cas3Application = null),
      )

      assertThat(result.url).isNull()
    }
  }

  @Test
  fun `null linkType leaves result unchanged`() {
    val original = buildServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
    val result = resolver.resolve(original, buildDomainData())

    assertThat(result).isSameAs(original)
  }
}
