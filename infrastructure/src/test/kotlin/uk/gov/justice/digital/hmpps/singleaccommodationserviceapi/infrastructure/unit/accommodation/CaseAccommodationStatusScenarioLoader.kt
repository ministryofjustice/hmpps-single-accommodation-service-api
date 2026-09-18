package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus

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
    val lines = CaseAccommodationStatusScenarioLoader::class.java
      .getResourceAsStream(CSV_RESOURCE_PATH)
      ?.bufferedReader()
      ?.use { it.readLines() }
      ?: error("CSV not found on classpath: $CSV_RESOURCE_PATH")

    require(lines.isNotEmpty()) { "CSV is empty: $CSV_RESOURCE_PATH" }

    val headers = splitCsvLine(lines.first(), expectedColumns = null)

    return lines
      .drop(1)
      .filter { it.isNotBlank() }
      .mapIndexed { rowIndex, line ->
        val values = splitCsvLine(line, expectedColumns = headers.size)
        val row = headers.zip(values).toMap()

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

  private fun splitCsvLine(line: String, expectedColumns: Int?): List<String> {
    val columns = line.split(',', ignoreCase = false, limit = expectedColumns ?: 0)

    if (expectedColumns != null && columns.size != expectedColumns) {
      error("Expected $expectedColumns columns but found ${columns.size} in line: $line")
    }

    return columns
  }

  private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

  private fun String.toCaseAccommodationStatusOrNull(): CaseAccommodationStatus? = takeUnless { it == "NULL" }?.let(CaseAccommodationStatus::valueOf)
}
