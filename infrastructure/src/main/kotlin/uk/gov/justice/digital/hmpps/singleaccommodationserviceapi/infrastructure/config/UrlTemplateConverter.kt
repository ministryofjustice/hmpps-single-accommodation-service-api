package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.stereotype.Component

@Component
class UrlTemplateConverter : GenericConverter {
  override fun getConvertibleTypes(): MutableSet<GenericConverter.ConvertiblePair> = mutableSetOf(GenericConverter.ConvertiblePair(String::class.java, UrlTemplate::class.java))

  override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any {
    val input = source as String
    return UrlTemplate(input)
  }
}
