package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation

enum class AddressStatusCode(val description: String) {
  B("Bail"),
  M("Main"),
  MA("Postal"),
  P("Previous"),

  /**
   * The next address when not currently in Prison
   */
  PR("Proposed"),

  /**
   * The next address on leaving Prison
   */
  PR1("Proposed for Resettlement"),
  RJ("Rejected"),
  RT("ROTL"),
  S("Secondary"),
  PM("Primary and Mail"),
  UNKNOWN("Unknown"),
}
