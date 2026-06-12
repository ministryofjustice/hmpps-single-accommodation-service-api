package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

enum class CasService(val urlPath: String) {
  CAS1("cas1"),
  CAS2HDC("cas2-hdc"),
  CAS2("cas2"),
  CAS3("cas3"),
}
