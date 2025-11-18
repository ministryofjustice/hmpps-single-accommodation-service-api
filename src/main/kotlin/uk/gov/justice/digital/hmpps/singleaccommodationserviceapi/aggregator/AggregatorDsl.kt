package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

class AggregatorDslBuilder(
  private val name: String,
  private val aggregatorRegistry: AggregatorRegistry,
) {
  private val endpointNames = mutableSetOf<String>()

  fun endpoint(name: String) {
    endpointNames.add(name)
  }

  fun endpoints(vararg names: String) {
    endpointNames.addAll(names)
  }

  fun build() {
    require(endpointNames.isNotEmpty()) { "Aggregator '$name' must have at least one endpoint" }
    aggregatorRegistry.register(name, endpointNames.toSet())
  }
}

fun AggregatorRegistry.aggregator(name: String, block: AggregatorDslBuilder.() -> Unit) {
  val builder = AggregatorDslBuilder(name, this)
  builder.block()
  builder.build()
}
