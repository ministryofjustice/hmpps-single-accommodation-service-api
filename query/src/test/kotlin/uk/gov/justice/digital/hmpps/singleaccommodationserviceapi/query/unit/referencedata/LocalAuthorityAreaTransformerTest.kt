package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaTransformer
import java.util.UUID

class LocalAuthorityAreaTransformerTest {

  @Nested
  inner class ToLocalAuthorityArea {

    @Test
    fun `should transform entity to LocalAuthorityArea with all fields`() {
      val id = UUID.randomUUID()

      val entity = buildLocalAuthorityAreaEntity(
        id = id,
        identifier = "E09000001",
        name = "City of London",
        active = true,
      )

      val result = LocalAuthorityAreaTransformer.toLocalAuthorityAreaDto(entity)

      assertThat(result.id).isEqualTo(id)
      assertThat(result.identifier).isEqualTo("E09000001")
      assertThat(result.name).isEqualTo("City of London")
      assertThat(result.active).isTrue()
    }

    @Test
    fun `should transform inactive entity correctly`() {
      val entity = buildLocalAuthorityAreaEntity(
        identifier = "S12000033",
        name = "Aberdeen City",
        active = false,
      )

      val result = LocalAuthorityAreaTransformer.toLocalAuthorityAreaDto(entity)

      assertThat(result.identifier).isEqualTo("S12000033")
      assertThat(result.name).isEqualTo("Aberdeen City")
      assertThat(result.active).isFalse()
    }
  }
}
