package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius

data class StaffDetail(
  val email: String?,
  val telephoneNumber: String?,
  val teams: List<Team> = emptyList(),
  val username: String?,
  val name: PersonName,
  val code: String,
  val active: Boolean,
)

data class PersonName(
  val forename: String,
  val surname: String,
  val middleName: String? = null,
)
