package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.ReferenceDataTransformer
import java.util.UUID

class ReferenceDataTransformerTest {

  @Nested
  inner class ToReferenceDataDto {

    @Test
    fun `should transform entity to ReferenceDataDto with all fields`() {
      val id = UUID.randomUUID()

      val entity = buildLocalAuthorityAreaEntity(
        id = id,
        code = "E09000001",
        name = "City of London",
      )

      val result = ReferenceDataTransformer.toReferenceDataDto(entity)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.name).isEqualTo("City of London")
      assertThat(result.code).isEqualTo("E09000001")
    }
  }
}
