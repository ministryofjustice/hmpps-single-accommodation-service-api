package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class Cas3PremisesSearchResults(
  val totalPremises: Int,
  val results: List<Cas3PremisesSearchResult>? = null,
  val totalOnlineBedspaces: Int? = null,
  val totalUpcomingBedspaces: Int? = null,
)

data class Cas3PremisesSearchResult(
  val id: UUID,
  val reference: String,
  val addressLine1: String,
  val postcode: String,
  val pdu: String,
  val addressLine2: String? = null,
  val town: String? = null,
  val localAuthorityAreaName: String? = null,
  val bedspaces: List<Cas3BedspacePremisesSearchResult>? = null,
  val totalArchivedBedspaces: Int? = null,
)


data class Cas3BedspacePremisesSearchResult(
  val id: UUID,
  val reference: String,
  val status: Cas3BedspaceStatus,
)


@Suppress("ktlint:standard:enum-entry-name-case", "EnumNaming")
enum class Cas3BedspaceStatus(@get:JsonValue val value: String) {

  online("online"),
  archived("archived"),
  upcoming("upcoming"),
  ;

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): Cas3BedspaceStatus = entries.first { it.value == value }
  }
}

