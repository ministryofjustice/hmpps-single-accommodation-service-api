package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

interface Rule {
  val services: List<ServiceType>
  val description: String
  fun evaluate(data: DomainData): RuleResult
}

enum class ServiceType(val value: String) {
  CAS1("CAS1"),
}
