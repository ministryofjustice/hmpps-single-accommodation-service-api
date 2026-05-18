package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation

data class AddressContact(
  val typeCode: ContactType,
  val value: String,
  val extension: String? = null,
)

enum class ContactType(val description: String) {
  HOME("Home"),
  BUS("Business"),
  FAX("Fax"),
  ALTB("Alternate Business"),
  ALTH("Alternate Home"),
  MOBILE("Mobile"),
  VISIT("Agency Visit Line"),
  EMAIL("Email"),
}
