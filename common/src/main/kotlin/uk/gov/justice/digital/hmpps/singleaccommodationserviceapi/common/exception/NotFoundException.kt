package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception

class NotFoundException(
  val entity: String,
  searchValues: Map<String, Any>,
) : RuntimeException("$entity not found for ${searchValues.entries}")

inline fun <reified T> T?.orThrowNotFound(
  vararg searchValues: Pair<String, Any>,
): T {
  require(searchValues.isNotEmpty())
  return this ?: throw NotFoundException(
    entity = T::class.simpleName ?: "Entity",
    searchValues = searchValues.toMap(),
  )
}
