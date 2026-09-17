package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util

inline fun requireXor(value1: Boolean, value2: Boolean, lazyMessage: () -> Any) {
  if (!(value1.xor(value2))) {
    val message = lazyMessage()
    throw IllegalArgumentException(message.toString())
  }
}
