package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStreamReader

class CsvReader {

  fun read(resource: String): List<Map<String, String>> {
    val input = this::class.java.getResourceAsStream(resource)
      ?: error("CSV not found: $resource")

    val result = CSVParser.builder()
      .setFormat(
        CSVFormat.DEFAULT.builder()
          .setHeader()
          .setTrim(true)
          .setNullString("") // this maps an empty value between commas (`,,`) as null
          .setSkipHeaderRecord(true)
          .get(),
      )
      .setReader(InputStreamReader(input))
      .get()

    return result.records.map { record ->
      record.toMap()
    }
  }
}
