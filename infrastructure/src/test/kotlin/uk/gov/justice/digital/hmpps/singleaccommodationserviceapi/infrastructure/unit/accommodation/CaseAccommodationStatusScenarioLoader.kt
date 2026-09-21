package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.utils.CsvReader

object CaseAccommodationStatusScenarioLoader {
  private const val CSV_RESOURCE_PATH = "/accommodation/CaseAccommodationStatusScenarios.csv"

  data class Scenario(
    val id: Int,
    val testName: String,
    val currentType: String?,
    val currentEndDate: String?,
    val nextType: String?,
    val nextEndDate: String?,
    val expectedStatus: CaseAccommodationStatus?,
  ) {
    override fun toString(): String = "[$id] ${displayLabel(currentType, currentEndDate)} -> ${displayLabel(nextType, nextEndDate)}"

    private companion object {
      fun displayLabel(type: String?, endDate: String?): String = when (type) {
        null -> "null"
        "HOMELESS" -> if (endDate == null) "homeless" else "homeless with end"
        "SETTLED" -> if (endDate == null) "settled" else "settled with end"
        "TRANSIENT" -> if (endDate == null) "transient" else "transient with end"
        "UNKNOWN" -> if (endDate == null) "unknown" else "unknown with end"
        else -> error("Unknown accommodation type name in CSV: $type")
      }
    }
  }

  fun loadScenarios(): List<Scenario> {
    val rows = CsvReader().read(CSV_RESOURCE_PATH)

    require(rows.isNotEmpty()) { "CSV is empty: $CSV_RESOURCE_PATH" }

    return rows.mapIndexed { rowIndex, row ->
      try {
        Scenario(
          id = row.getValue("id").toInt(),
          testName = row.getValue("testName"),
          currentType = row.getValue("currentType").nullIfBlank(),
          currentEndDate = row.getValue("currentEndDate").nullIfBlank(),
          nextType = row.getValue("nextType").nullIfBlank(),
          nextEndDate = row.getValue("nextEndDate").nullIfBlank(),
          expectedStatus = row.getValue("expectedStatus").toCaseAccommodationStatusOrNull(),
        )
      } catch (e: Exception) {
        throw IllegalStateException("Failed to parse CSV row ${rowIndex + 2} from $CSV_RESOURCE_PATH: $row", e)
      }
    }
  }

  private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

  private fun String.toCaseAccommodationStatusOrNull(): CaseAccommodationStatus? = takeUnless { it == "NULL" }?.let(CaseAccommodationStatus::valueOf)
}
