package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2ServiceStatusTransformer
import java.util.UUID

class Cas2ServiceStatusTransformerTest {

  @Nested
  inner class Cas2HdcApplicationToServiceStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2HdcApplication = Cas2HdcApplication(
        id = UUID.randomUUID(),
      )
      assertThat(Cas2ServiceStatusTransformer.toServiceStatus(cas2HdcApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }

  @Nested
  inner class Cas2CourtBailApplicationToServiceStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2CourtBailApplication = Cas2CourtBailApplication(
        id = UUID.randomUUID(),
      )
      assertThat(Cas2ServiceStatusTransformer.toServiceStatus(cas2CourtBailApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }

  @Nested
  inner class Cas2PrisonBailApplicationToServiceStatus {

    @Test
    fun `transform to serviceStatus`() {
      val cas2PrisonBailApplication = Cas2PrisonBailApplication(
        id = UUID.randomUUID(),
      )
      assertThat(Cas2ServiceStatusTransformer.toServiceStatus(cas2PrisonBailApplication)).isEqualTo(ServiceStatus.NOT_STARTED)
    }
  }
}
