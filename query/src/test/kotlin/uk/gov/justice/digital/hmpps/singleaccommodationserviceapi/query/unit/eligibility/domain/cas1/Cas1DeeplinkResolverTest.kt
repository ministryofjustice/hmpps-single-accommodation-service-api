package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.util.UUID

class Cas1DeeplinkResolverTest {
  private val cas1UiUrl = "CAS1_UI_URL"
  private val resolver = Cas1DeeplinkResolver(cas1UiUrl)

  @Nested
  inner class ResolveTests {
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

    @Test
    fun `null linkType leaves result unchanged`() {
      val original = buildServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
      val result = resolver.resolve(original, buildDomainData())

      assertThat(result).isSameAs(original)
    }
  }
}
