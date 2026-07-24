package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

class TitleEnumSerialiser : ValueSerializer<TitleEnum>() {
  override fun serialize(
    value: TitleEnum?,
    gen: JsonGenerator?,
    ctxt: SerializationContext?,
  ) {
    gen?.writeString(value?.title)
  }
}
