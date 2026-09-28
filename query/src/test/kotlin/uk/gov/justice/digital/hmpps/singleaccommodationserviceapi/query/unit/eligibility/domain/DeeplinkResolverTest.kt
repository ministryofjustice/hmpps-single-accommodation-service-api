package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew

class DeeplinkResolverTest {
  private val cas1ApplicationStartUrl = "CAS1_APPLICATION_START_URL"
  private val cas1ApplicationViewUrl = "CAS1_APPLICATION_VIEW_URL"
  private val cas2ApplicationStartUrl = "CAS2_APPLICATION_START_URL"
  private val cas2ApplicationViewUrl = "CAS2_APPLICATION_VIEW_URL"
  private val cas3ReferralStartUrl = "CAS3_REFERRAL_START_URL"
  private val cas3ReferralViewUrl = "CAS3_REFERRAL_VIEW_URL"
  private val approvedPremisesCachingService = mockk<ApprovedPremisesCachingService> {
    every { getCas1UrlTemplates() } returns Cas1UrlTemplates(cas1ApplicationStartUrl)
    every { getCas2UrlTemplates() } returns Cas2UrlTemplates(cas2ApplicationStartUrl)
    every { getCas3UrlTemplates() } returns Cas3UrlTemplates(cas3ReferralStartUrl)
  }
  private val resolver = DeeplinkResolver(approvedPremisesCachingService)

  @Nested
  inner class Cas1 {
    @Test
    fun `CAS1_START_APPLICATION resolves to the CAS1 application start url template`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS1_START_APPLICATION),
        buildDomainData(),
      )

      assertThat(result.url).isEqualTo(cas1ApplicationStartUrl)
    }

    @Test
    fun `CAS1_START_APPLICATION resolves to a null url when the url templates fetch fails`() {
      every { approvedPremisesCachingService.getCas1UrlTemplates() } throws RuntimeException("boom")

      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS1_START_APPLICATION),
        buildDomainData(),
      )

      assertThat(result.url).isNull()
    }

    @Test
    fun `CAS1_VIEW_APPLICATION resolves to the application uiUrl`() {
      val cas1Application = buildCas1Application(
        uiUrl = cas1ApplicationViewUrl,
      )
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS1_VIEW_APPLICATION),
        buildDomainData(cas1Application = cas1Application),
      )

      assertThat(result.url).isEqualTo(cas1ApplicationViewUrl)
    }

    @Test
    fun `CAS1_VIEW_APPLICATION with no application resolves to null url`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS1_VIEW_APPLICATION),
        buildDomainData(cas1Application = null),
      )

      assertThat(result.url).isNull()
    }
  }

  @Nested
  inner class Cas2 {
    @Test
    fun `CAS2_START_APPLICATION resolves to the CAS2 application start url template`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS2_START_APPLICATION),
        buildDomainData(),
      )

      assertThat(result.url).isEqualTo(cas2ApplicationStartUrl)
    }

    @Test
    fun `CAS2_START_APPLICATION resolves to a null url when the url templates fetch fails`() {
      every { approvedPremisesCachingService.getCas2UrlTemplates() } throws RuntimeException("boom")

      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS2_START_APPLICATION),
        buildDomainData(),
      )

      assertThat(result.url).isNull()
    }

    @Test
    fun `CAS2_VIEW_APPLICATION resolves to the application uiUrl`() {
      val cas2Application = buildCas2Application(
        uiUrl = cas2ApplicationViewUrl,
      )
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS2_VIEW_APPLICATION),
        buildDomainData(cas2Application = cas2Application),
      )

      assertThat(result.url).isEqualTo(cas2ApplicationViewUrl)
    }

    @Test
    fun `CAS2_VIEW_APPLICATION with no application resolves to null url`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS2_VIEW_APPLICATION),
        buildDomainData(cas2Application = null),
      )

      assertThat(result.url).isNull()
    }
  }

  @Nested
  inner class Cas3 {
    @Test
    fun `CAS3_START_REFERRAL resolves to the CAS3 referral start url template`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS3_START_REFERRAL),
        buildDomainData(),
      )

      assertThat(result.url).isEqualTo(cas3ReferralStartUrl)
    }

    @Test
    fun `CAS3_START_REFERRAL resolves to a null url when the url templates fetch fails`() {
      every { approvedPremisesCachingService.getCas3UrlTemplates() } throws RuntimeException("boom")

      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS3_START_REFERRAL),
        buildDomainData(),
      )

      assertThat(result.url).isNull()
    }

    @Test
    fun `CAS3_VIEW_REFERRAL resolves to the application uiUrl`() {
      val cas3Application = buildCas3Application(
        uiUrl = cas3ReferralViewUrl,
      )
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS3_VIEW_REFERRAL),
        buildDomainData(cas3Application = cas3Application),
      )

      assertThat(result.url).isEqualTo(cas3ReferralViewUrl)
    }

    @Test
    fun `CAS3_VIEW_REFERRAL with no application resolves to null url`() {
      val result = resolver.resolve(
        buildServiceResultNew(linkType = LinkType.CAS3_VIEW_REFERRAL),
        buildDomainData(cas3Application = null),
      )

      assertThat(result.url).isNull()
    }
  }

  @Test
  fun `null linkType leaves result unchanged`() {
    val original = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_ELIGIBLE)
    val result = resolver.resolve(original, buildDomainData())

    assertThat(result).isSameAs(original)
  }
}
